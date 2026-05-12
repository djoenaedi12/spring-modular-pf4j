package gasi.gps.core.api.security;

/**
 * Exception raised when an authentication provider cannot find a principal.
 *
 * @since 1.0.0
 */
public class AuthProviderUserNotFoundException extends AuthProviderException {

    /**
     * Creates an exception with a user-not-found message.
     *
     * @param message error message
     */
    public AuthProviderUserNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a user-not-found message and cause.
     *
     * @param message error message
     * @param cause   underlying cause
     */
    public AuthProviderUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
