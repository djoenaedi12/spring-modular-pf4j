package gasi.gps.core.api.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service class for automatic entity audit logging.
 *
 * <p>Place this annotation on application services that perform create, update,
 * or delete operations for a resource. The audit implementation lives outside
 * {@code core-api} and decides how to intercept the service.</p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code @AuditableEntity(module = "hr", resourceType = "Employee")}
 * public class EmployeeService { }
 * </pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableEntity {

    /**
     * Returns the module used for grouping audit records.
     *
     * @return module code, for example {@code AUTH}, {@code PAYROLL}, or
     *         {@code LEAVE}
     */
    String module();

    /**
     * Returns the audited resource type.
     *
     * @return resource type, for example {@code Employee} or {@code User}
     */
    String resourceType() default "";

    /**
     * Returns the CRUD actions that should be logged.
     *
     * @return action codes to audit
     */
    String[] auditActions() default { "CREATE", "UPDATE", "DELETE" };

    /**
     * Controls whether nested service calls still create audit records.
     *
     * @return {@code true} to always write an audit record for this resource
     */
    boolean alwaysLog() default false;
}
