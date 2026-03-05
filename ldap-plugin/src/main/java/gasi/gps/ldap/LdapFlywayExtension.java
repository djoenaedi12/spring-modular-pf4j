package gasi.gps.ldap;

import org.pf4j.Extension;

import gasi.gps.core.api.infrastructure.FlywayMigrationExtension;

/**
 * PF4J extension that registers LDAP plugin Flyway migrations.
 */
@Extension
public class LdapFlywayExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
