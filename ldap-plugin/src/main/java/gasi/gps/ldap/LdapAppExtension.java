package gasi.gps.ldap;

import org.pf4j.Extension;

import gasi.gps.core.api.AppExtension;

/**
 * PF4J extension providing LDAP plugin metadata to the core application.
 */
@Extension
public class LdapAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "ldap-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "LDAP authentication provider for auth plugin.";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }
}
