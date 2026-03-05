package gasi.gps.core.api.infrastructure.security;

/**
 * SPI for extracting current user information from the security context.
 * <p>
 * This interface is meant to be implemented by an authentication plugin
 * (e.g. JWT, OAuth2, etc.) and registered as a Spring bean so that
 * core modules can obtain the current user's identity without coupling
 * to a specific auth mechanism.
 */
public interface SecurityContextUtil {

    /**
     * Get current authenticated user's username.
     *
     * @return username, or {@code null} if not authenticated
     */
    String getCurrentUsername();

    /**
     * Get current user's role / primary authority.
     *
     * @return role string, or {@code null} if unavailable
     */
    String getCurrentUserRole();

    /**
     * Get client IP address from the current HTTP request.
     *
     * @return IP address, or {@code null} if unavailable
     */
    String getCurrentIp();

    /**
     * Get user agent from the current HTTP request.
     *
     * @return user agent string, or {@code null} if unavailable
     */
    String getCurrentUserAgent();
}
