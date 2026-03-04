package gasi.gps.audit;

import org.pf4j.Extension;

import gasi.gps.core.api.infrastructure.FlywayMigrationExtension;

/**
 * PF4J extension that registers the audit plugin's Flyway migration location.
 */
@Extension
public class AuditFlywayExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
