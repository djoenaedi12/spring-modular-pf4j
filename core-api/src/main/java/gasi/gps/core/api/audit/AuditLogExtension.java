package gasi.gps.core.api.audit;

import org.pf4j.ExtensionPoint;

/**
 * PF4J extension point for plugins to enrich audit log descriptions.
 * Plugins can implement this to provide module-specific human-readable
 * descriptions.
 */
public interface AuditLogExtension extends ExtensionPoint {

    /**
     * Module identifier this enricher supports.
     * Example: "gps-payroll", "gpc-hris"
     */
    String supportedModule();

    /**
     * Provide a custom description for the audit log entry.
     *
     * @param action       the action (CREATE, UPDATE, DELETE, etc.)
     * @param resourceType the resource type name
     * @param resourceId   the resource ID
     * @return custom description, or null to use default
     */
    String resolveDescription(String action, String resourceType, String resourceId);
}
