package {{FULL_PACKAGE}}.extension;

import org.pf4j.Extension;

import gasi.gps.core.api.migration.FlywayMigrationExtension;

/**
 * Registers the Flyway migration location for plugin {{PLUGIN_ID}}
 * so that it is included by the core application during startup.
 *
 * <p>Migration location: {@code classpath:{{FLYWAY_LOCATION}}}.</p>
 */
@Extension
public class {{FLYWAY_EXT_CLASS_NAME}} implements FlywayMigrationExtension {

    @Override
    public String getMigrationLocation() {
        return "classpath:{{FLYWAY_LOCATION}}";
    }
}
