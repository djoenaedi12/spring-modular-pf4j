package gasi.gps.core.api.security;

/**
 * Base exception for provider-level authentication errors.
 *
 * @since 1.0.0
 */
public class AuthProviderException extends RuntimeException {

    /**
     * Creates an exception with an authentication provider message.
     *
     * @param message error message
     */
    public AuthProviderException(String message) {
        super(message);
    }

    /**
     * Creates an exception with an authentication provider message and cause.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public AuthProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
