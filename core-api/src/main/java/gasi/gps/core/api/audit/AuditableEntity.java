package gasi.gps.core.api.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level annotation to mark a service as auditable.
 * Place on service classes that extend BaseService to automatically
 * generate audit logs for CUD operations.
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code @AuditableEntity(category = "EMPLOYEE", resourceType = "Employee")}
 * public class EmployeeService extends BaseService<Employee, Long> { }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableEntity {

    /**
     * Business module for grouping logs.
     * Example: EMPLOYEE, PAYROLL, LEAVE, AUTH
     */
    String module();

    /**
     * Human-readable entity name for the log.
     * Example: Employee, LeaveRequest, PayrollRun
     */
    String resourceType() default "";

    /**
     * Which CUD actions to audit.
     * Default: CREATE, UPDATE, DELETE
     */
    String[] auditActions() default { "CREATE", "UPDATE", "DELETE" };

    /**
     * If true, this entity will always be logged even when called
     * from within another audited service (nested call).
     * Use for sensitive entities like BankAccount that require
     * their own audit trail regardless of context.
     */
    boolean alwaysLog() default false;
}
