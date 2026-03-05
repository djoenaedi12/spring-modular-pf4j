package gasi.gps.core.api.infrastructure.security;

import java.util.Map;

/**
 * Authentication input payload for pluggable identity providers.
 *
 * @param principal  user identifier (username, email, UPN, etc.)
 * @param secret     credential secret (password/token/assertion)
 * @param attributes optional provider-specific attributes
 */
public record AuthProviderCredentials(
        String principal,
        String secret,
        Map<String, String> attributes) {

    /**
     * Creates credentials with no extra attributes.
     *
     * @param principal user identifier
     * @param secret    credential secret
     * @return credentials instance
     */
    public static AuthProviderCredentials of(String principal, String secret) {
        return new AuthProviderCredentials(principal, secret, Map.of());
    }
}
