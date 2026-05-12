package gasi.gps.core.api.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method-level business operation for audit logging.
 *
 * <p>This annotation is intended for operations that are not covered by generic
 * CRUD auditing, such as approve, reject, export, login, or sync.</p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code @Auditable(action = "APPROVE", module = "LEAVE",
 *           description = "Approve leave request #{#id}")}
 * public LeaveRequest approveLeave(Long id) { }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Returns the business action to write into the audit log.
     *
     * @return action code, for example {@code APPROVE}, {@code EXPORT}, or
     *         {@code LOGIN}
     */
    String action();

    /**
     * Returns the module that owns the audited operation.
     *
     * @return module code, or an empty string when the auditing infrastructure
     *         should infer it
     */
    String module() default "";

    /**
     * Returns the human-readable audit description template.
     *
     * <p>Implementations may support expression placeholders such as
     * {@code #{#paramName}} or {@code #{#result.field}}.</p>
     *
     * @return description template, or an empty string when no description is
     *         provided
     */
    String description() default "";

    /**
     * Controls whether this operation is logged inside another audited call.
     *
     * @return {@code true} to always log nested calls
     */
    boolean alwaysLog() default false;
}
