package gasi.gps.core.api.infrastructure;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for plugins that provide Flyway SQL migrations.
 *
 * <p>
 * Each plugin that needs database migrations must implement this interface.
 * The core application discovers all implementations via PF4J, builds a
 * composite {@link ClassLoader} from their classloaders, and runs a single
 * Flyway instance against the declared migration location.
 * </p>
 *
 * <p>
 * By convention, all plugins use the same flat location
 * ({@code classpath:db/migration}) so there is no need for per-plugin
 * subfolders. Simply place SQL scripts under:
 *
 * <pre>{@code src/main/resources/db/migration/}</pre>
 *
 * using timestamp-based versioning:
 *
 * <pre>{@code
 * V < YYYYMMDDHHmmss > __description.sql
 * }</pre>
 *
 * Example: {@code V20260303142600__user_init.sql}
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
     * The default value is {@code "classpath:db/migration"}, which is the
     * shared convention for all plugins. Override only if you have a specific
     * reason to use a different location.
     * </p>
     *
     * @return the migration location relative to the plugin's classpath
     */
    default String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
