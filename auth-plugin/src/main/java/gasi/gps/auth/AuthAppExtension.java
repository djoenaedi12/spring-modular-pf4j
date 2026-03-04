package gasi.gps.auth;

import org.pf4j.Extension;

import gasi.gps.core.api.AppExtension;

/**
 * PF4J extension providing auth plugin metadata to the core application.
 */
@Extension
public class AuthAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "auth-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "JWT authentication, user management, and RBAC.";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }
}
