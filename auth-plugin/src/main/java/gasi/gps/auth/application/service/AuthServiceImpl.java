package gasi.gps.auth.application.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.dto.ForgotPasswordRequest;
import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.application.dto.ResetPasswordRequest;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.domain.model.PasswordHistory;
import gasi.gps.auth.domain.model.PasswordReset;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.model.UserDevice;
import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.auth.domain.port.inbound.UserSessionService;
import gasi.gps.auth.domain.port.outbound.AppClientRepositoryPort;
import gasi.gps.auth.domain.port.outbound.PasswordHistoryRepositoryPort;
import gasi.gps.auth.domain.port.outbound.PasswordResetRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserDeviceRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.auth.infrastructure.security.JwtUtil;
import gasi.gps.auth.infrastructure.security.provider.LocalAuthProvider;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.audit.Auditable;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SimpleFilter.FilterOperator;
import gasi.gps.core.api.infrastructure.security.AuthProviderCredentials;
import gasi.gps.core.api.infrastructure.security.AuthProviderExtension;
import gasi.gps.core.api.infrastructure.security.AuthProviderUserNotFoundException;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;
import gasi.gps.core.api.infrastructure.util.HashUtil;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String LDAP_PROVIDER_ID = "ldap";
    private static final long RESET_TOKEN_TTL_SECONDS = 30 * 60;
    private static final int PASSWORD_HISTORY_KEEP_ROWS = 5;

    private final UserRepositoryPort userRepositoryPort;
    private final AppClientRepositoryPort appClientRepositoryPort;
    private final PasswordHistoryRepositoryPort passwordHistoryRepositoryPort;
    private final PasswordResetRepositoryPort passwordResetRepositoryPort;
    private final UserSessionService userSessionService;
    private final UserDeviceRepositoryPort userDeviceRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Map<String, AuthProviderExtension> providers;

    public AuthServiceImpl(UserRepositoryPort userRepositoryPort,
            AppClientRepositoryPort appClientRepositoryPort,
            PasswordHistoryRepositoryPort passwordHistoryRepositoryPort,
            PasswordResetRepositoryPort passwordResetRepositoryPort,
            UserSessionService userSessionService,
            UserDeviceRepositoryPort userDeviceRepositoryPort,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            List<AuthProviderExtension> providers) {
        this.userRepositoryPort = userRepositoryPort;
        this.appClientRepositoryPort = appClientRepositoryPort;
        this.passwordHistoryRepositoryPort = passwordHistoryRepositoryPort;
        this.passwordResetRepositoryPort = passwordResetRepositoryPort;
        this.userSessionService = userSessionService;
        this.userDeviceRepositoryPort = userDeviceRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                        provider -> normalizeProvider(provider.getProviderId()),
                        Function.identity(),
                        (first, second) -> first));
    }

    @Override
    @Auditable(action = "LOGIN", module = "Auth", description = "User login: #{#request.username}")
    public LoginResponse login(LoginRequest request) {
        AuthenticatedPrincipal principal = authenticateByPolicy(request.getProvider(), request.getUsername(),
                request.getPassword());

        String resolvedUsername = principal.username() != null
                ? principal.username()
                : request.getUsername();

        User user = userRepositoryPort.findByUsername(resolvedUsername)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        Set<String> persistedRoles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        Set<String> roles = principal.roles() != null && !principal.roles().isEmpty()
                ? principal.roles()
                : persistedRoles;

        String accessTokenJti = java.util.UUID.randomUUID().toString();
        String refreshTokenJti = java.util.UUID.randomUUID().toString();

        long accessExpiry = jwtUtil.getAccessTokenExpiration();
        long refreshExpiry = jwtUtil.getRefreshTokenExpiration();

        String accessToken = jwtUtil.generateAccessTokenWithJti(
                user.getUsername(), roles, null, accessExpiry, accessTokenJti);
        String refreshToken = jwtUtil.generateRefreshTokenWithJti(refreshExpiry, refreshTokenJti);

        Instant now = Instant.now();

        UserDevice userDevice = saveDevice(user, null, request, null);
        saveSession(user, null, request, userDevice, accessTokenJti, refreshTokenJti, refreshExpiry, now);

        user.setLastLoginAt(now);
        user.setFailedLoginCount(0);
        userRepositoryPort.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .build();
    }

    @Override
    @Auditable(action = "FORGOT_PASSWORD", module = "Auth", description = "Forgot password request: #{#request.username}")
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepositoryPort.findByUsername(request.getUsername())
                .ifPresent(this::generateAndSendResetToken);
    }

    @Override
    @Auditable(action = "RESET_PASSWORD", module = "Auth", description = "Password reset executed")
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        String tokenHash = HashUtil.sha256Base64(request.getToken());

        PasswordReset passwordReset = passwordResetRepositoryPort.findActiveByTokenHash(tokenHash, now)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        User user = passwordReset.getUser();
        validatePasswordNotReused(user, user.getPasswordHash(), request.getNewPassword());

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setPasswordChangedAt(now);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepositoryPort.save(user);

        passwordHistoryRepositoryPort.save(PasswordHistory.builder()
                .user(user)
                .passwordHash(newPasswordHash)
                .build());
        prunePasswordHistory(user);

        invalidateActiveResetTokens(user);
    }

    @Override
    @Auditable(action = "LOGIN", module = "Auth", description = "Client login: #{#clientId}, user: #{#request.username}")
    public LoginResponse login(String clientId, String clientSecret, LoginRequest request) {
        if (clientId == null || clientSecret == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        AppClient appClient = appClientRepositoryPort
                .findBy(new SimpleFilter("clientId", FilterOperator.EQUALS, clientId))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(clientSecret, appClient.getClientSecret())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (appClient.getGrantTypes() == null
                || Arrays.stream(appClient.getGrantTypes()).noneMatch(request.getGrantType()::equalsIgnoreCase)) {
            throw new BadCredentialsException("Unauthorized grant type: " + request.getGrantType());
        }

        AuthenticatedPrincipal principal = authenticateByPolicy(request.getProvider(), request.getUsername(),
                request.getPassword());

        String resolvedUsername = principal.username() != null
                ? principal.username()
                : request.getUsername();

        User user = userRepositoryPort.findByUsername(resolvedUsername)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        Set<String> persistedRoles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        Set<String> roles = principal.roles() != null && !principal.roles().isEmpty()
                ? principal.roles()
                : persistedRoles;

        Set<String> scopes = appClient.getScopes() != null ? Set.of(appClient.getScopes()) : Set.of();

        long accessExpiry = appClient.getAccessTokenValidity() != null ? appClient.getAccessTokenValidity()
                : jwtUtil.getAccessTokenExpiration();
        long refreshExpiry = appClient.getRefreshTokenValidity() != null ? appClient.getRefreshTokenValidity()
                : jwtUtil.getRefreshTokenExpiration();

        String accessTokenJti = java.util.UUID.randomUUID().toString();
        String refreshTokenJti = java.util.UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessTokenWithJti(
                user.getUsername(), roles, scopes, accessExpiry, accessTokenJti);
        String refreshToken = jwtUtil.generateRefreshTokenWithJti(refreshExpiry,
                refreshTokenJti);

        Instant now = Instant.now();

        UserDevice userDevice = saveDevice(user, appClient, request, null);
        saveSession(user, appClient, request, userDevice, accessTokenJti, refreshTokenJti, refreshExpiry, now);

        user.setLastLoginAt(now);
        user.setFailedLoginCount(0);
        userRepositoryPort.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .scope(scopes.isEmpty() ? null : String.join(" ", scopes))
                .build();
    }

    @Override
    @Auditable(action = "LOGOUT", module = "Auth", description = "User logout")
    public void logout(String accessTokenJti) {
        userSessionService.revokeAccessToken(accessTokenJti);
    }

    private void saveSession(User user, AppClient appClient, LoginRequest request, UserDevice userDevice,
            String accessTokenJti,
            String refreshTokenJti, long sessionExpirySeconds, Instant now) {
        UserSession.UserSessionBuilder builder = UserSession.builder()
                .user(user)
                .accessTokenJti(accessTokenJti)
                .refreshTokenJti(refreshTokenJti)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(sessionExpirySeconds))
                .lastActivityAt(now)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent());

        if (appClient != null) {
            builder.appClient(appClient);
        }
        if (userDevice != null) {
            builder.userDevice(userDevice);
        }

        userSessionService.save(builder.build(), accessTokenJti);
    }

    private UserDevice saveDevice(User user, AppClient appClient, LoginRequest request, Instant trustedExpiresAt) {
        String deviceId = request.getDeviceId();
        String deviceModel = request.getDeviceModel();
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
        }

        UserDevice.UserDeviceBuilder builder = UserDevice.builder()
                .user(user)
                .appClient(appClient)
                .deviceId(deviceId)
                .deviceModel(deviceModel)
                .trustedExpiresAt(trustedExpiresAt);
        return userDeviceRepositoryPort.save(builder.build());
    }

    private void generateAndSendResetToken(User user) {
        Instant now = Instant.now();
        invalidateActiveResetTokens(user);

        String rawToken = generateResetToken();
        Instant expiresAt = now.plusSeconds(RESET_TOKEN_TTL_SECONDS);

        PasswordReset resetEntity = PasswordReset.builder()
                .user(user)
                .resetTokenHash(HashUtil.sha256Base64(rawToken))
                .expiresAt(expiresAt)
                .build();

        passwordResetRepositoryPort.save(resetEntity);

        String recipient = (user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : user.getUsername();

        passwordResetRepositoryPort.sendPasswordReset(recipient, rawToken, expiresAt);
    }

    private void invalidateActiveResetTokens(User user) {
        GenericFilter activeUserFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("user.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(user.getId())
                                .build(),
                        SimpleFilter.builder()
                                .field("usedAt")
                                .operator(SimpleFilter.FilterOperator.IS_NULL)
                                .build()))
                .build();
        passwordResetRepositoryPort.deleteAllBy(activeUserFilter, false);
    }

    private void validatePasswordNotReused(User user, String currentPasswordHash, String newPassword) {
        if (currentPasswordHash != null && passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new BusinessException("New password has been used previously");
        }

        List<PasswordHistory> histories = passwordHistoryRepositoryPort.findByUserIdOrderByCreatedAtDesc(user.getId());
        boolean alreadyUsed = histories.stream()
                .map(PasswordHistory::getPasswordHash)
                .filter(hash -> hash != null && !hash.isBlank())
                .anyMatch(hash -> passwordEncoder.matches(newPassword, hash));

        if (alreadyUsed) {
            throw new BusinessException("New password has been used previously");
        }
    }

    private void prunePasswordHistory(User user) {
        int keepRows = Math.max(PASSWORD_HISTORY_KEEP_ROWS, 0);
        List<PasswordHistory> histories = passwordHistoryRepositoryPort.findByUserIdOrderByCreatedAtDesc(user.getId());

        if (histories.size() <= keepRows) {
            return;
        }

        List<Long> idsToDelete = histories.subList(keepRows, histories.size()).stream()
                .map(PasswordHistory::getId)
                .filter(id -> id != null)
                .toList();

        if (idsToDelete.isEmpty()) {
            return;
        }

        passwordHistoryRepositoryPort.deleteAllByIds(idsToDelete);
    }

    private String generateResetToken() {
        String partOne = UUID.randomUUID().toString().replace("-", "");
        String partTwo = UUID.randomUUID().toString().replace("-", "");
        return partOne + partTwo;
    }

    private AuthenticatedPrincipal authenticateByPolicy(String provider, String username, String password) {
        AuthProviderCredentials credentials = AuthProviderCredentials.of(username, password);

        if (provider != null && !provider.isBlank()) {
            return resolveProvider(provider).authenticate(credentials);
        }

        AuthProviderExtension ldapProvider = providers.get(LDAP_PROVIDER_ID);
        if (ldapProvider != null) {
            try {
                return ldapProvider.authenticate(credentials);
            } catch (AuthProviderUserNotFoundException ex) {
                AuthProviderExtension localProvider = resolveProvider(LocalAuthProvider.PROVIDER_ID);
                return localProvider.authenticate(credentials);
            }
        }

        return resolveProvider(LocalAuthProvider.PROVIDER_ID).authenticate(credentials);
    }

    private AuthProviderExtension resolveProvider(String provider) {
        String normalizedProvider = normalizeProvider(provider);
        AuthProviderExtension authProvider = providers.get(normalizedProvider);
        if (authProvider == null) {
            throw new BusinessException("Unsupported auth provider: " + normalizedProvider);
        }
        return authProvider;
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return LocalAuthProvider.PROVIDER_ID;
        }
        return provider.trim().toLowerCase(Locale.ROOT);
    }
}
