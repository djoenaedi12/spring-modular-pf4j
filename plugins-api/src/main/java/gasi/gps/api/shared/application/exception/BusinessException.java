package gasi.gps.api.shared.application.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when one or more business rules are violated.
 * Collects all errors before throwing, so the client gets all issues at once.
 */
public class BusinessException extends RuntimeException {

    private final List<String> errors;

    public BusinessException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public BusinessException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * Helper to collect multiple validations before throwing.
     */
    public static class Collector {
        private final List<String> errors = new ArrayList<>();

        public Collector add(String error) {
            errors.add(error);
            return this;
        }

        public Collector addIf(boolean condition, String error) {
            if (condition) {
                errors.add(error);
            }
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * Throws BusinessException if any errors were collected.
         */
        public void validate() {
            if (hasErrors()) {
                throw new BusinessException(errors);
            }
        }
    }
}
