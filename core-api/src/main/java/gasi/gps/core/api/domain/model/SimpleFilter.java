package gasi.gps.core.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Filter expression for a single searchable field.
 *
 * <p>The {@code field} value is a public API field name. Implementations may
 * restrict which fields are accepted, for example by requiring entity fields to
 * be explicitly marked as filterable.</p>
 *
 * @since 1.0.0
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

    /**
     * Supported operators for {@link SimpleFilter}.
     */
    public enum FilterOperator {
        /** Field value must equal {@code value}. */
        EQUALS,
        /** Field value must not equal {@code value}. */
        NOT_EQUALS,
        /** Field value must be greater than {@code value}. */
        GREATER_THAN,
        /** Field value must be greater than or equal to {@code value}. */
        GREATER_THAN_OR_EQUALS,
        /** Field value must be less than {@code value}. */
        LESS_THAN,
        /** Field value must be less than or equal to {@code value}. */
        LESS_THAN_OR_EQUALS,
        /** Field value must match a contains-style string pattern. */
        LIKE,
        /** Field value must be included in a collection value. */
        IN,
        /** Field value must be {@code null}. */
        IS_NULL,
        /** Field value must not be {@code null}. */
        IS_NOT_NULL
    }
}
