package gasi.gps.audit;

/**
 * ThreadLocal context to control audit logging in nested service calls.
 *
 * <p>When a top-level audited method is executing, nested calls to other
 * audited services will be skipped (unless marked with alwaysLog = true).</p>
 *
 * <p>This prevents duplicate logs when, for example, EmployeeService.create()
 * internally calls UserService.create() and RoleService.create().</p>
 */
public final class AuditContext {

    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    private AuditContext() {
    }

    /**
     * Check if an audit context is already active (we're inside a nested call).
     */
    public static boolean isActive() {
        return ACTIVE.get();
    }

    /**
     * Start the audit context. Called by the first (top-level) audited method.
     */
    public static void start() {
        ACTIVE.set(true);
    }

    /**
     * Clear the audit context. Must be called in a finally block.
     */
    public static void clear() {
        ACTIVE.remove();
    }
}
