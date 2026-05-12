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

import io.jsonwebtoken.JwtException;

import gasi.gps.auth.application.dto.ForgotPasswordRequest;
import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.application.dto.RefreshTokenRequest;
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
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SimpleFilter.FilterOperator;
import gasi.gps.core.api.security.AuthProviderCredentials;
import gasi.gps.core.api.security.AuthProviderExtension;
import gasi.gps.core.api.security.AuthProviderUserNotFoundException;
import gasi.gps.core.api.security.AuthenticatedPrincipal;
import gasi.gps.core.starter.infrastructure.util.HashUtil;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String LDAP_PROVIDER_ID = "ldap";
    private static final long RESET_TOKEN_TTL_SECONDS = 30 * 60;
    private static final int PASSWORD_HISTORY_KEEP_ROWS = 5;
    private static final int DEFAULT_MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long DEFAULT_LOCKOUT_SECONDS = 15 * 60;

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
        AuthenticatedPrincipal principal = authenticateOrRecordFailure(
                null, request.getProvider(), request.getUsername(), request.getPassword());
        User user = resolveAuthenticatedUser(principal, request.getUsername());
        return issueTokens(user, principal, null, request, Set.of(),
                jwtUtil.getAccessTokenExpiration(), jwtUtil.getRefreshTokenExpiration());
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

        markActiveResetTokensUsed(user, now);
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

        AuthenticatedPrincipal principal = authenticateOrRecordFailure(
                appClient, request.getProvider(), request.getUsername(), request.getPassword());
        User user = resolveAuthenticatedUser(principal, request.getUsername());

        Set<String> scopes = appClient.getScopes() != null ? Set.of(appClient.getScopes()) : Set.of();

        long accessExpiry = appClient.getAccessTokenValidity() != null ? appClient.getAccessTokenValidity()
                : jwtUtil.getAccessTokenExpiration();
        long refreshExpiry = appClient.getRefreshTokenValidity() != null ? appClient.getRefreshTokenValidity()
                : jwtUtil.getRefreshTokenExpiration();

        return issueTokens(user, principal, appClient, request, scopes, accessExpiry, refreshExpiry);
    }

    @Override
    @Auditable(action = "REFRESH_TOKEN", module = "Auth", description = "Refresh access token")
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshTokenJti;
        try {
            refreshTokenJti = jwtUtil.getJtiFromRefreshToken(request.getRefreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        UserSession session = userSessionService.findActiveByRefreshToken(refreshTokenJti)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        AppClient appClient = session.getAppClient();
        if (appClient != null && (appClient.getGrantTypes() == null
                || Arrays.stream(appClient.getGrantTypes()).noneMatch("refresh_token"::equalsIgnoreCase))) {
            throw new BadCredentialsException("Unauthorized grant type: refresh_token");
        }

        userSessionService.revokeAccessToken(session.getAccessTokenJti());

        LoginRequest loginRequest = LoginRequest.builder()
                .deviceId(session.getUserDevice() != null ? session.getUserDevice().getDeviceId() : null)
                .deviceModel(session.getUserDevice() != null ? session.getUserDevice().getDeviceModel() : null)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build();

        Set<String> scopes = appClient != null && appClient.getScopes() != null
                ? Set.of(appClient.getScopes())
                : Set.of();
        long accessExpiry = appClient != null && appClient.getAccessTokenValidity() != null
                ? appClient.getAccessTokenValidity()
                : jwtUtil.getAccessTokenExpiration();
        long refreshExpiry = appClient != null && appClient.getRefreshTokenValidity() != null
                ? appClient.getRefreshTokenValidity()
                : jwtUtil.getRefreshTokenExpiration();

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                LocalAuthProvider.PROVIDER_ID,
                null,
                session.getUser().getUsername(),
                Set.of(),
                Map.of());

        return issueTokens(session.getUser(), principal, appClient, loginRequest,
                scopes, accessExpiry, refreshExpiry);
    }

    @Override
    @Auditable(action = "LOGOUT", module = "Auth", description = "User logout")
    public void logout(String accessTokenJti) {
        userSessionService.revokeAccessToken(accessTokenJti);
    }

    private User resolveAuthenticatedUser(AuthenticatedPrincipal principal, String requestedUsername) {
        String resolvedUsername = principal.username() != null
                ? principal.username()
                : requestedUsername;
        return userRepositoryPort.findByUsername(resolvedUsername)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    private LoginResponse issueTokens(User user, AuthenticatedPrincipal principal, AppClient appClient,
            LoginRequest request, Set<String> scopes, long accessExpiry, long refreshExpiry) {
        ensureUserCanAuthenticate(user);
        Set<String> roles = resolveRoles(user, principal);
        boolean refreshTokenAllowed = canIssueRefreshToken(appClient);
        String accessTokenJti = UUID.randomUUID().toString();
        String refreshTokenJti = refreshTokenAllowed ? UUID.randomUUID().toString() : null;
        String accessToken = jwtUtil.generateAccessTokenWithJti(
                user.getUsername(), roles, scopes, accessExpiry, accessTokenJti);
        String refreshToken = refreshTokenAllowed
                ? jwtUtil.generateRefreshTokenWithJti(refreshExpiry, refreshTokenJti)
                : null;
        Instant now = Instant.now();
        long sessionExpiry = refreshTokenAllowed ? refreshExpiry : accessExpiry;

        UserDevice userDevice = saveDevice(user, appClient, request, null);
        saveSession(user, appClient, request, userDevice, accessTokenJti, refreshTokenJti, sessionExpiry, now);

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

    private boolean canIssueRefreshToken(AppClient appClient) {
        return appClient == null || (appClient.getGrantTypes() != null
                && Arrays.stream(appClient.getGrantTypes()).anyMatch("refresh_token"::equalsIgnoreCase));
    }

    private Set<String> resolveRoles(User user, AuthenticatedPrincipal principal) {
        if (principal.roles() != null && !principal.roles().isEmpty()) {
            return principal.roles();
        }
        return user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();
    }

    private AuthenticatedPrincipal authenticateOrRecordFailure(AppClient appClient, String provider, String username,
            String password) {
        try {
            return authenticateByPolicy(provider, username, password);
        } catch (BadCredentialsException ex) {
            recordFailedLogin(username, appClient);
            throw ex;
        }
    }

    private void recordFailedLogin(String username, AppClient appClient) {
        if (username == null || username.isBlank()) {
            return;
        }

        userRepositoryPort.findByUsername(username).ifPresent(user -> {
            int maxAttempts = resolveMaxFailedLoginAttempts(appClient);
            long lockoutSeconds = resolveLockoutSeconds(appClient);
            int failedCount = user.getFailedLoginCount() != null ? user.getFailedLoginCount() : 0;
            int nextFailedCount = failedCount + 1;
            user.setFailedLoginCount(nextFailedCount);
            if (nextFailedCount >= maxAttempts && lockoutSeconds > 0) {
                user.setLockedUntil(Instant.now().plusSeconds(lockoutSeconds));
            }
            userRepositoryPort.save(user);
        });
    }

    private int resolveMaxFailedLoginAttempts(AppClient appClient) {
        if (appClient == null || appClient.getMaxFailedLoginAttempts() == null) {
            return DEFAULT_MAX_FAILED_LOGIN_ATTEMPTS;
        }
        return Math.max(1, appClient.getMaxFailedLoginAttempts());
    }

    private long resolveLockoutSeconds(AppClient appClient) {
        if (appClient == null || appClient.getLockoutSeconds() == null) {
            return DEFAULT_LOCKOUT_SECONDS;
        }
        return Math.max(0, appClient.getLockoutSeconds());
    }

    private void ensureUserCanAuthenticate(User user) {
        Instant now = Instant.now();
        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new BadCredentialsException("User account is disabled");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new BadCredentialsException("User account is locked");
        }
        if (user.getAuthorizedUntil() != null && user.getAuthorizedUntil().isBefore(now)) {
            throw new BadCredentialsException("User account authorization has expired");
        }
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
        markActiveResetTokensUsed(user, now);

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

    private void markActiveResetTokensUsed(User user, Instant usedAt) {
        List<PasswordReset> activeTokens = passwordResetRepositoryPort.findActiveByUserId(user.getId(), usedAt);
        for (PasswordReset token : activeTokens) {
            token.setUsedAt(usedAt);
            passwordResetRepositoryPort.save(token);
        }
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
