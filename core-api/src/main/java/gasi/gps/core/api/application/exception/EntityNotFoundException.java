package gasi.gps.core.api.application.exception;

/**
 * Exception raised when a requested resource cannot be found.
 *
 * @since 1.0.0
 */
public class EntityNotFoundException extends RuntimeException {
    /**
     * Creates an exception with a not-found message.
     *
     * @param message not-found error message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
