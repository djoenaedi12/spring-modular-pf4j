package gasi.gps.audit;

import org.pf4j.Extension;

import gasi.gps.core.api.AppExtension;

/**
 * PF4J extension providing audit plugin metadata to the core application.
 */
@Extension
public class AuditAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "audit-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Provides automatic audit logging for CUD operations and custom method-level auditing.";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }
}
