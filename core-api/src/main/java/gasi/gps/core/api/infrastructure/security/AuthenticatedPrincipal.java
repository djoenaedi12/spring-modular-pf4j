package gasi.gps.core.api.infrastructure.security;

import java.util.Map;
import java.util.Set;

/**
 * Normalized principal returned by an authentication provider.
 *
 * @param providerId source provider identifier (local/ldap/azuread/saml)
 * @param externalId provider-side immutable identifier, if available
 * @param username   normalized application username
 * @param roles      normalized authority codes from provider (optional)
 * @param attributes additional provider claims/attributes (optional)
 */
public record AuthenticatedPrincipal(
        String providerId,
        String externalId,
        String username,
        Set<String> roles,
        Map<String, String> attributes) {

    /**
     * Creates a principal with empty roles and attributes.
     *
     * @param providerId source provider identifier
     * @param externalId provider-side immutable identifier
     * @param username   normalized application username
     * @return principal instance
     */
    public static AuthenticatedPrincipal of(String providerId, String externalId, String username) {
        return new AuthenticatedPrincipal(providerId, externalId, username, Set.of(), Map.of());
    }
}
