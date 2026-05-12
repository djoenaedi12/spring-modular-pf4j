package gasi.gps.core.starter.infrastructure.filter;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity field as available for API filtering and sorting.
 *
 * <p>Fields without this annotation are rejected by the generic search
 * implementation. Use {@link #alias()} to expose a stable public field name
 * when the Java entity field should remain internal.</p>
 *
 * @since 1.0.0
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filterable {

    /**
     * Public API field name. When empty, the Java entity field name is used.
     *
     * @return public API field name, or an empty string to use the entity field
     *         name
     */
    String alias() default "";
}
