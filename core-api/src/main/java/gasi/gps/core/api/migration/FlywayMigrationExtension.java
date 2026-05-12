package gasi.gps.core.api.migration;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for plugins that provide Flyway SQL migrations.
 *
 * <p>
 * Each plugin that needs database migrations must implement this interface.
 * The host application discovers all implementations via PF4J, builds a
 * composite {@link ClassLoader} from their classloaders, and runs a single
 * Flyway instance against the declared migration location.
 * </p>
 *
 * <p>
 * By convention, each plugin owns a module-specific location such as
 * {@code classpath:db/migration/auth}. Place SQL scripts under a matching
 * folder, for example {@code src/main/resources/db/migration/auth/}, and use
 * timestamp-based versions such as {@code V20260303142600__user_init.sql}.
 * Versions should be globally unique across all plugins.
 * </p>
 *
 * @see org.pf4j.ExtensionPoint
 * @since 1.0.0
 */
public interface FlywayMigrationExtension extends ExtensionPoint {

    /**
     * Returns the classpath location of the SQL migration scripts for this plugin.
     *
     * <p>
     * Use a module-specific folder, for example
     * {@code "classpath:db/migration/auth"}.
     * </p>
     *
     * @return the migration location relative to the plugin's classpath
     */
    String getMigrationLocation();
}
