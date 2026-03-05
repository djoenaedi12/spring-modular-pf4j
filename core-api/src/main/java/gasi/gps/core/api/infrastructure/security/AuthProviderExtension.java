package gasi.gps.core.api.infrastructure.security;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for pluggable authentication providers.
 *
 * <p>
 * Implementations can authenticate against local database, LDAP, OIDC, SAML,
 * or other identity systems, then return a normalized principal.
 * </p>
 */
public interface AuthProviderExtension extends ExtensionPoint {

    /**
     * Returns provider identifier (for example: local, ldap, azuread, saml).
     *
     * @return provider id
     */
    String getProviderId();

    /**
     * Authenticates incoming credentials.
     *
     * @param credentials authentication payload
     * @return normalized authenticated principal
     * @throws AuthProviderUserNotFoundException when principal does not exist in
     *                                           provider store
     * @throws AuthProviderException             for other provider authentication
     *                                           failures
     */
    AuthenticatedPrincipal authenticate(AuthProviderCredentials credentials);
}
