package gasi.gps.core.infrastructure.config;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import gasi.gps.core.api.infrastructure.FlywayMigrationExtension;

/**
 * Configuration that runs Flyway database migrations for the core application
 * and all plugins implementing {@link FlywayMigrationExtension}.
 *
 * <p>
 * Uses a <strong>single Flyway instance</strong> backed by a composite
 * {@link ClassLoader} built from the core application and all plugin
 * classloaders. Migration locations are collected from each plugin via
 * {@link FlywayMigrationExtension#getMigrationLocation()}.
 * </p>
 *
 * <p>
 * By convention, all plugins return {@code "classpath:db/migration"} so there
 * is a single, flat folder — no per-plugin subfolders needed. Scripts are
 * ordered globally by their timestamp-based version
 * ({@code V<YYYYMMDDHHmmss>__description.sql}).
 * </p>
 *
 * @see FlywayMigrationExtension
 * @since 1.0.0
 */
@Configuration
public class PluginFlywayConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PluginFlywayConfig.class);

    /**
     * Creates a {@link CommandLineRunner} that executes Flyway migrations from
     * all registered locations using a composite plugin classloader.
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
            List<FlywayMigrationExtension> extensions = pluginManager.getExtensions(FlywayMigrationExtension.class);

            Set<String> locations = new LinkedHashSet<>();
            locations.add("classpath:db/migration"); // core-app

            ClassLoader compositeClassLoader = buildCompositeClassLoader(pluginManager, extensions, locations);

            LOG.info("[Flyway] Running migrations from {} location(s): {}", locations.size(), locations);

            Flyway flyway = Flyway.configure(compositeClassLoader)
                    .dataSource(dataSource)
                    .locations(locations.toArray(new String[0]))
                    .table("flyway_schema_history")
                    .mixed(true)
                    .baselineOnMigrate(true)
                    .load();

            int applied = flyway.migrate().migrationsExecuted;
            LOG.info("[Flyway] Migration selesai. {} script dijalankan.", applied);
        };
    }

    /**
     * Builds a composite {@link ClassLoader} combining the core classloader with
     * each plugin's classloader. Also collects unique migration locations from
     * each plugin's {@link FlywayMigrationExtension#getMigrationLocation()}.
     */
    private ClassLoader buildCompositeClassLoader(
            PluginManager pluginManager,
            List<FlywayMigrationExtension> extensions,
            Set<String> locations) {

        List<URL> urls = new ArrayList<>();
        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        for (FlywayMigrationExtension ext : extensions) {
            String pluginId = resolvePluginId(pluginManager, ext);
            String location = ext.getMigrationLocation();
            locations.add(location);

            ClassLoader pluginCl = pluginManager.getPluginClassLoader(pluginId);
            if (pluginCl instanceof URLClassLoader urlCl) {
                for (URL url : urlCl.getURLs()) {
                    urls.add(url);
                    LOG.debug("[Flyway] Added classpath URL from plugin '{}': {}", pluginId, url);
                }
                LOG.info("[Flyway] Plugin '{}' registered → location: {}", pluginId, location);
            } else {
                LOG.warn("[Flyway] Plugin '{}' classloader is not a URLClassLoader, skipping: {}",
                        pluginId, pluginCl != null ? pluginCl.getClass().getName() : "null");
            }
        }

        return urls.isEmpty() ? parent
                : new URLClassLoader(urls.toArray(new URL[0]), parent);
    }

    /**
     * Resolves the plugin ID for a given extension instance by matching its
     * classloader.
     */
    private String resolvePluginId(PluginManager pluginManager, FlywayMigrationExtension ext) {
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            ClassLoader cl = pluginManager.getPluginClassLoader(wrapper.getPluginId());
            if (cl != null && ext.getClass().getClassLoader() == cl) {
                return wrapper.getPluginId();
            }
        }
        return "unknown";
    }
}
