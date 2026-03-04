package gasi.gps.core.api.audit;

/**
 * Port interface for plugins that need to write audit logs manually
 * (outside of the automatic AOP mechanism).
 */
public interface AuditLogSpi {

    /**
     * Write a manual audit log entry.
     *
     * @param action      the action (CREATE, UPDATE, DELETE, EXPORT, etc.)
     * @param category    business category
     * @param entityType  entity type name
     * @param entityId    entity identifier
     * @param description human-readable description
     */
    void log(String action, String category, String entityType, String entityId, String description);

    /**
     * Write a manual audit log entry with module info.
     */
    void log(String action, String category, String module, String entityType, String entityId, String description);
}
