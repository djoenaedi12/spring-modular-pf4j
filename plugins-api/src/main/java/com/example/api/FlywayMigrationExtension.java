package com.example.api;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for registering Flyway migration locations from a plugin.
 *
 * <p>
 * Each plugin that needs database migrations must implement this interface
 * and provide the classpath location of its SQL migration scripts. The core
 * application collects all registered locations and runs them through a single
 * Flyway instance with a shared {@code flyway_schema_history} table.
 * </p>
 *
 * <p>
 * <strong>Version range convention:</strong>
 * </p>
 * <ul>
 * <li>core-app &mdash; V1 – V999</li>
 * <li>inventory &mdash; V1001 – V1999</li>
 * <li>product &mdash; V2001 – V2999</li>
 * </ul>
 *
 * @see org.pf4j.ExtensionPoint
 * @see com.example.core.config.PluginFlywayConfig
 * @since 1.0.0
 */
public interface FlywayMigrationExtension extends ExtensionPoint {

    /**
     * Returns the classpath location of the SQL migration scripts for this plugin.
     *
     * <p>
     * Example: {@code "classpath:db/migration/inventory"}
     * </p>
     *
     * @return the migration location relative to the plugin's classpath
     */
    String getMigrationLocation();
}
