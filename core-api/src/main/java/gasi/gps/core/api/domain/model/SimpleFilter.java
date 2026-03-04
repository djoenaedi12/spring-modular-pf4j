package gasi.gps.core.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A single field filter condition.
 * Example: name EQUALS "iPhone", price GREATER_THAN 1000
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SimpleFilter extends GenericFilter {
    private String field;
    private FilterOperator operator;
    private Object value;

    public enum FilterOperator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        LIKE,
        IN,
        IS_NULL,
        IS_NOT_NULL
    }
}
