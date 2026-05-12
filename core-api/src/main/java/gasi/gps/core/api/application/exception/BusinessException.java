package gasi.gps.core.api.application.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception raised when one or more business rules are violated.
 *
 * <p>The exception can carry multiple messages so validation logic can return
 * all detected issues instead of failing on the first one.</p>
 *
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private final List<String> errors;

    /**
     * Creates an exception with one business error message.
     *
     * @param message business error message
     */
    public BusinessException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    /**
     * Creates an exception with multiple business error messages.
     *
     * @param errors business error messages
     */
    public BusinessException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns all business error messages.
     *
     * @return immutable list of errors
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Collects validation errors before throwing one {@link BusinessException}.
     */
    public static class Collector {
        private final List<String> errors = new ArrayList<>();

        /**
         * Adds an error message.
         *
         * @param error business error message
         * @return this collector
         */
        public Collector add(String error) {
            errors.add(error);
            return this;
        }

        /**
         * Adds an error message when a condition is {@code true}.
         *
         * @param condition condition that indicates a validation failure
         * @param error     business error message
         * @return this collector
         */
        public Collector addIf(boolean condition, String error) {
            if (condition) {
                errors.add(error);
            }
            return this;
        }

        /**
         * Indicates whether any errors were collected.
         *
         * @return {@code true} when at least one error exists
         */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * Throws {@link BusinessException} when any errors were collected.
         */
        public void validate() {
            if (hasErrors()) {
                throw new BusinessException(errors);
            }
        }
    }
}
