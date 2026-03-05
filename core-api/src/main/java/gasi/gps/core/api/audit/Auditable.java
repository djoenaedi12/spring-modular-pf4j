package gasi.gps.core.api.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level annotation for auditing non-CRUD operations
 * like APPROVE, REJECT, EXPORT, LOGIN, etc.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code @Auditable(action = "APPROVE", category = "LEAVE",
 *           description = "Approve leave request #{#id}")}
 * public LeaveRequest approveLeave(Long id) { }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Action type: APPROVE, REJECT, EXPORT, LOGIN, LOGOUT, etc.
     */
    String action();

    /**
     * Business category.
     */
    String category() default "";

    /**
     * Description with optional SpEL expressions.
     * Supports: #{#paramName}, #{#result.field}
     */
    String description() default "";

    /**
     * If true, always log even when nested inside another audited call.
     */
    boolean alwaysLog() default false;
}
