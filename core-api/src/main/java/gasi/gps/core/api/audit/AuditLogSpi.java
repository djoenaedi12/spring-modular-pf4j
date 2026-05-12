package gasi.gps.core.api.audit;

/**
 * Port for writing audit logs outside of automatic auditing.
 *
 * <p>Use this SPI for explicit business events that are not naturally covered
 * by CRUD interception, such as login, export, approval, or integration
 * callbacks.</p>
 *
 * @since 1.0.0
 */
public interface AuditLogSpi {

    /**
     * Write a manual audit log entry.
     *
     * @param action      the action, for example {@code CREATE}, {@code EXPORT},
     *                    or {@code LOGIN}
     * @param category    business category for grouping logs
     * @param entityType  entity type name
     * @param entityId    entity identifier, or {@code null} for non-entity events
     * @param description human-readable description
     */
    void log(String action, String category, String entityType, String entityId, String description);

    /**
     * Write a manual audit log entry with module info.
     *
     * @param action      the action, for example {@code CREATE}, {@code EXPORT},
     *                    or {@code LOGIN}
     * @param category    business category for grouping logs
     * @param module      module code that owns the event
     * @param entityType  entity type name
     * @param entityId    entity identifier, or {@code null} for non-entity events
     * @param description human-readable description
     */
    void log(String action, String category, String module, String entityType, String entityId, String description);
}
