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
import gasi.gps.auth.domain.port.outbound.AppClientRepositoryPort;
import gasi.gps.auth.domain.port.outbound.PasswordHistoryRepositoryPort;
import gasi.gps.auth.domain.port.outbound.PasswordResetRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserSessionRepositoryPort;
import gasi.gps.auth.infrastructure.security.JwtUtil;
import gasi.gps.auth.infrastructure.security.provider.LocalAuthProvider;
import gasi.gps.core.api.application.exception.BusinessException;
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
        private final UserSessionRepositoryPort userSessionRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Map<String, AuthProviderExtension> providers;

    public AuthServiceImpl(UserRepositoryPort userRepositoryPort,
            AppClientRepositoryPort appClientRepositoryPort,
            PasswordHistoryRepositoryPort passwordHistoryRepositoryPort,
            PasswordResetRepositoryPort passwordResetRepositoryPort,
            UserSessionRepositoryPort userSessionRepositoryPort,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            List<AuthProviderExtension> providers) {
        this.userRepositoryPort = userRepositoryPort;
        this.appClientRepositoryPort = appClientRepositoryPort;
        this.passwordHistoryRepositoryPort = passwordHistoryRepositoryPort;
        this.passwordResetRepositoryPort = passwordResetRepositoryPort;
        this.userSessionRepositoryPort = userSessionRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                        provider -> normalizeProvider(provider.getProviderId()),
                        Function.identity(),
                        (first, second) -> first));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        AuthenticatedPrincipal principal = authenticateByPolicy(request.getProvider(), request.getUsername(),
                request.getPassword());

        String resolvedUsername = principal.username() != null
                ? principal.username()
                : request.getUsername();

        User user = userRepositoryPort.findByUsername(resolvedUsername)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        Set<String> persistedRoles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        Set<String> roles = principal.roles() != null && !principal.roles().isEmpty()
                ? principal.roles()
                : persistedRoles;

        String accessTokenJti = java.util.UUID.randomUUID().toString();
        String refreshTokenJti = java.util.UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessTokenWithJti(
                user.getUsername(), roles, null, jwtUtil.getAccessTokenExpiration(),
                accessTokenJti);
        String refreshToken = jwtUtil.generateRefreshTokenWithJti(jwtUtil.getRefreshTokenExpiration(), refreshTokenJti);

        // Update last login
        user.setLastLoginAt(Instant.now());
        user.setFailedLoginCount(0);
        userRepositoryPort.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepositoryPort.findByUsername(request.getUsername())
                .ifPresent(this::generateAndSendResetToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        String tokenHash = HashUtil.sha256Base64(request.getToken());

        PasswordReset passwordReset = passwordResetRepositoryPort.findActiveByTokenHash(tokenHash, now)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        Long userId = passwordReset.getUserId();
        User user = userRepositoryPort.findById(userId, false)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        validatePasswordNotReused(userId, user.getPasswordHash(), request.getNewPassword());

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setPasswordChangedAt(now);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepositoryPort.save(user);

        passwordHistoryRepositoryPort.save(PasswordHistory.builder()
                .userId(userId)
                .passwordHash(newPasswordHash)
                .build());
        prunePasswordHistory(userId);

        invalidateActiveResetTokens(userId);
    }

    @Override
    public LoginResponse login(String clientId, String clientSecret, LoginRequest request) {
        if (clientId == null || clientSecret == null) {
            throw new BusinessException("Client credentials are required");
        }

        AppClient client = appClientRepositoryPort.findBy(new SimpleFilter("clientId", FilterOperator.EQUALS, clientId))
                .orElseThrow(() -> new BusinessException("Invalid client credentials"));

        if (!clientSecret.equals(client.getClientSecret())) {
            throw new BusinessException("Invalid client credentials");
        }

        if (client.getGrantTypes() == null
                || Arrays.stream(client.getGrantTypes()).noneMatch(request.getGrantType()::equalsIgnoreCase)) {
            throw new BusinessException("Unauthorized grant type: " + request.getGrantType());
        }

        AuthenticatedPrincipal principal = authenticateByPolicy(request.getProvider(), request.getUsername(),
                request.getPassword());

        String resolvedUsername = principal.username() != null
                ? principal.username()
                : request.getUsername();

        User user = userRepositoryPort.findByUsername(resolvedUsername)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        Set<String> persistedRoles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        Set<String> roles = principal.roles() != null && !principal.roles().isEmpty()
                ? principal.roles()
                : persistedRoles;

        Set<String> scopes = client.getScopes() != null ? Set.of(client.getScopes()) : Set.of();

        long accessExpiry = client.getAccessTokenValidity() != null ? client.getAccessTokenValidity()
                : jwtUtil.getAccessTokenExpiration();

        // Use 86400 (24h) or another default if refresh token expires not in jwtUtil
        long refreshExpiry = client.getRefreshTokenValidity() != null ? client.getRefreshTokenValidity()
                : jwtUtil.getRefreshTokenExpiration();

        String accessTokenJti = java.util.UUID.randomUUID().toString();
        String refreshTokenJti = java.util.UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessTokenWithJti(
                user.getUsername(), roles, scopes, accessExpiry, accessTokenJti);
        String refreshToken = jwtUtil.generateRefreshTokenWithJti(refreshExpiry,
                refreshTokenJti);

        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plusSeconds(refreshExpiry);

        userSessionRepositoryPort.save(UserSession.builder()
                .user(User.builder().id(user.getId()).build())
                .appClient(AppClient.builder().id(client.getId()).build())
                .userDevice(UserDevice.builder()
                        .deviceId(request.getDeviceId())
                        .deviceModel(request.getDeviceModel())
                        .build())
                .accessTokenJti(accessTokenJti)
                .refreshTokenJti(refreshTokenJti)
                .issuedAt(now)
                .expiresAt(sessionExpiresAt)
                .lastActivityAt(now)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .build());

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

    private void generateAndSendResetToken(User user) {
        Instant now = Instant.now();
        invalidateActiveResetTokens(user.getId());

        String rawToken = generateResetToken();
        Instant expiresAt = now.plusSeconds(RESET_TOKEN_TTL_SECONDS);

        PasswordReset resetEntity = PasswordReset.builder()
                .userId(user.getId())
                .resetTokenHash(HashUtil.sha256Base64(rawToken))
                .expiresAt(expiresAt)
                .build();

        passwordResetRepositoryPort.save(resetEntity);

        String recipient = (user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : user.getUsername();

        passwordResetRepositoryPort.sendPasswordReset(recipient, rawToken, expiresAt);
    }

    private void invalidateActiveResetTokens(Long userId) {
        GenericFilter activeUserFilter = AndFilter.builder()
                .filters(List.of(
                        SimpleFilter.builder()
                                .field("user.id")
                                .operator(SimpleFilter.FilterOperator.EQUALS)
                                .value(userId)
                                .build(),
                        SimpleFilter.builder()
                                .field("usedAt")
                                .operator(SimpleFilter.FilterOperator.IS_NULL)
                                .build()))
                .build();
        passwordResetRepositoryPort.deleteAllBy(activeUserFilter, false);
    }

    private void validatePasswordNotReused(Long userId, String currentPasswordHash, String newPassword) {
        if (currentPasswordHash != null && passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new BusinessException("New password has been used previously");
        }

        List<PasswordHistory> histories = passwordHistoryRepositoryPort.findByUserIdOrderByCreatedAtDesc(userId);
        boolean alreadyUsed = histories.stream()
                .map(PasswordHistory::getPasswordHash)
                .filter(hash -> hash != null && !hash.isBlank())
                .anyMatch(hash -> passwordEncoder.matches(newPassword, hash));

        if (alreadyUsed) {
            throw new BusinessException("New password has been used previously");
        }
    }

    private void prunePasswordHistory(Long userId) {
        int keepRows = Math.max(PASSWORD_HISTORY_KEEP_ROWS, 0);
        List<PasswordHistory> histories = passwordHistoryRepositoryPort.findByUserIdOrderByCreatedAtDesc(userId);

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
