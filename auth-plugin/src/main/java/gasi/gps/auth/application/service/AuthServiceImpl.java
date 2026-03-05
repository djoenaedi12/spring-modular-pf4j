package gasi.gps.auth.application.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.auth.domain.port.outbound.AppClientRepositoryPort;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.auth.infrastructure.security.JwtUtil;
import gasi.gps.auth.infrastructure.security.provider.LocalAuthProvider;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.api.domain.model.SimpleFilter.FilterOperator;
import gasi.gps.core.api.infrastructure.security.AuthProviderCredentials;
import gasi.gps.core.api.infrastructure.security.AuthProviderExtension;
import gasi.gps.core.api.infrastructure.security.AuthProviderUserNotFoundException;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String LDAP_PROVIDER_ID = "ldap";

    private final UserRepositoryPort userRepository;
    private final AppClientRepositoryPort appClientRepository;
    private final JwtUtil jwtUtil;
    private final Map<String, AuthProviderExtension> providers;

    public AuthServiceImpl(UserRepositoryPort userRepository,
            AppClientRepositoryPort appClientRepository,
            JwtUtil jwtUtil,
            List<AuthProviderExtension> providers) {
        this.userRepository = userRepository;
        this.appClientRepository = appClientRepository;
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

        User user = userRepository.findByUsername(resolvedUsername)
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
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public LoginResponse login(String clientId, String clientSecret, LoginRequest request) {
        if (clientId == null || clientSecret == null) {
            throw new BusinessException("Client credentials are required");
        }

        AppClient client = appClientRepository.findBy(new SimpleFilter("clientId", FilterOperator.EQUALS, clientId))
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

        User user = userRepository.findByUsername(resolvedUsername)
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

        user.setLastLoginAt(Instant.now());
        user.setFailedLoginCount(0);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .scope(scopes.isEmpty() ? null : String.join(" ", scopes))
                .build();
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
