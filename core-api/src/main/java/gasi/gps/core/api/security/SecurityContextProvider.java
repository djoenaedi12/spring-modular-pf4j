package gasi.gps.core.api.security;

/**
 * Contract for reading the current authenticated request context.
 *
 * <p>Authentication plugins implement this provider so core infrastructure can
 * access user and request metadata without depending on a concrete security
 * mechanism such as JWT, LDAP, OAuth2, or SAML.</p>
 *
 * @since 1.0.0
 */
public interface SecurityContextProvider {

    /**
     * Returns the current authenticated user's username.
     *
     * @return username, or {@code null} if not authenticated
     */
    String getCurrentUsername();

    /**
     * Returns the current user's primary role or authority.
     *
     * @return role string, or {@code null} if unavailable
     */
    String getCurrentUserRole();

    /**
     * Returns the client IP address from the current request.
     *
     * @return IP address, or {@code null} if unavailable
     */
    String getCurrentIp();

    /**
     * Returns the user agent from the current request.
     *
     * @return user agent string, or {@code null} if unavailable
     */
    String getCurrentUserAgent();
}
