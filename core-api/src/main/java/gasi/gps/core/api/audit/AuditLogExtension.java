package gasi.gps.core.api.audit;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for enriching audit log descriptions.
 *
 * <p>Plugins implement this contract when the default audit description is not
 * expressive enough for a module-specific resource or workflow. The audit
 * infrastructure may call all registered enrichers and use the first
 * non-{@code null} description.</p>
 *
 * @see AuditLogSpi
 * @since 1.0.0
 */
public interface AuditLogExtension extends ExtensionPoint {

    /**
     * Returns the module identifier supported by this enricher.
     *
     * @return module code, for example {@code "auth"} or {@code "audit"}
     */
    String supportedModule();

    /**
     * Provide a custom description for the audit log entry.
     *
     * @param action       the audited action, for example {@code CREATE} or
     *                     {@code APPROVE}
     * @param resourceType the resource type name
     * @param resourceId   the resource identifier, usually the database ID as a
     *                     string
     * @return custom description, or {@code null} to use the default description
     */
    String resolveDescription(String action, String resourceType, String resourceId);
}
