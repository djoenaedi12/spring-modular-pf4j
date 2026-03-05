package gasi.gps.core.api.infrastructure.security;

/**
 * Raised when a provider cannot find the principal in its identity store.
 */
public class AuthProviderUserNotFoundException extends AuthProviderException {

    public AuthProviderUserNotFoundException(String message) {
        super(message);
    }

    public AuthProviderUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
