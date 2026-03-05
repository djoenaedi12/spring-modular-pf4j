package gasi.gps.core.api.infrastructure.security;

/**
 * Base exception for provider-level authentication errors.
 */
public class AuthProviderException extends RuntimeException {

    public AuthProviderException(String message) {
        super(message);
    }

    public AuthProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
