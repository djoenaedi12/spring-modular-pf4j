package com.example.core.config;

import com.example.api.FlywayMigrationExtension;
import org.flywaydb.core.Flyway;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration that runs Flyway database migrations for the core application
 * and all plugins implementing {@link FlywayMigrationExtension}.
 *
 * <p>
 * Uses a <strong>single Flyway instance</strong> with merged migration
 * locations and a shared {@code flyway_schema_history} table.
 * </p>
 *
 * <p>
 * <strong>Version range convention:</strong>
 * </p>
 * <ul>
 * <li>core-app &mdash; V1 – V999 ({@code classpath:db/migration/core})</li>
 * <li>inventory &mdash; V1001 – V1999
 * ({@code classpath:db/migration/inventory})</li>
 * <li>product &mdash; V2001 – V2999
 * ({@code classpath:db/migration/product})</li>
 * </ul>
 *
 * @see FlywayMigrationExtension
 * @since 1.0.0
 */
@Configuration
public class PluginFlywayConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PluginFlywayConfig.class);

    /**
     * Creates a {@link CommandLineRunner} that executes Flyway migrations from
     * all registered locations (core + plugins).
     *
     * <p>
     * Ordered with {@code @Order(1)} to ensure migrations complete before
     * Hibernate schema validation runs.
     * </p>
     *
     * @param dataSource    the application's {@link DataSource}
     * @param pluginManager the PF4J plugin manager for discovering migration
     *                      extensions
     * @return a runner that performs the combined Flyway migration
     */
    @Bean
    @Order(1)
    public CommandLineRunner runPluginMigrations(DataSource dataSource, PluginManager pluginManager) {
        return args -> {
            // Kumpulkan semua lokasi migration: core + semua plugin
            List<String> locations = new ArrayList<>();
            locations.add("classpath:db/migration/core");

            List<FlywayMigrationExtension> extensions = pluginManager.getExtensions(FlywayMigrationExtension.class);

            for (FlywayMigrationExtension ext : extensions) {
                String location = ext.getMigrationLocation();
                locations.add(location);
                LOG.info("[Flyway] Plugin migration location registered: {}", location);
            }

            LOG.info("[Flyway] Running migrations from {} location(s): {}", locations.size(), locations);

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations.toArray(new String[0]))
                    .table("flyway_schema_history") // satu tabel untuk semua
                    .mixed(true) // boleh campuran SQL + Java
                    .baselineOnMigrate(true) // handle DB yang sudah ada sebelum Flyway
                    .load();

            int applied = flyway.migrate().migrationsExecuted;
            LOG.info("[Flyway] Migration selesai. {} script dijalankan.", applied);
        };
    }
}
