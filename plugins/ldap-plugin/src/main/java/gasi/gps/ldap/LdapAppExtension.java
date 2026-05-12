package gasi.gps.ldap;

import java.util.List;

import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;

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

    @Override
    public List<String> getBasePackages() {
        return List.of("gasi.gps.ldap");
    }
}
