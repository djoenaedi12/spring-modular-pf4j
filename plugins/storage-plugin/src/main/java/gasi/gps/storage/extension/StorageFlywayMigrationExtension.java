package gasi.gps.storage.extension;

import org.pf4j.Extension;
import gasi.gps.core.api.migration.FlywayMigrationExtension;

/**
 * Registers the Flyway migration location for plugin storage-plugin
 * so that it is included by the core application during startup.
 *
 * <p>Migration location: {@code classpath:db/migration/storage}.</p>
 */
@Extension
public class StorageFlywayMigrationExtension implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration/storage";
    }
}
