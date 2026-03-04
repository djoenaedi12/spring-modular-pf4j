package gasi.gps.auth;

import org.pf4j.Extension;

import gasi.gps.core.api.infrastructure.FlywayMigrationExtension;

/**
 * PF4J extension that registers the auth plugin's Flyway migration location.
 */
@Extension
public class AuthFlywayExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
