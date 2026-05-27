package gasi.gps.platform.infrastructure.config;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import gasi.gps.core.api.migration.FlywayMigrationExtension;

/**
 * Configuration that runs Flyway database migrations for the platform application
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
 * By convention, each module owns a dedicated migration location such as
 * {@code "classpath:db/migration/platform"} or
 * {@code "classpath:db/migration/auth"}. Scripts are ordered globally by their
 * timestamp-based version
 * ({@code V<YYYYMMDDHHmmss>__description.sql}).
 * </p>
 *
 * @see FlywayMigrationExtension
 * @since 1.0.0
 */
@Configuration
public class PluginFlywayConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PluginFlywayConfig.class);
    private static final String PLATFORM_MIGRATION_LOCATION = "classpath:db/migration/platform";

    /**
     * Creates a {@link CommandLineRunner} that executes Flyway migrations from
     * all registered locations using a composite plugin classloader.
     *
     * <p>
     * Ordered with {@code @Order(1)} to ensure migrations complete before
     * Hibernate schema validation runs.
     * </p>
     *
     * @param dataSource         the application's {@link DataSource}
     * @param pluginManager      the PF4J plugin manager for discovering migration
     *                           extensions
     * @param schemaHistoryTable Flyway schema history table name
     * @return a runner that performs the combined Flyway migration
     */
    @Bean
    @Order(1)
    public CommandLineRunner runPluginMigrations(
            DataSource dataSource,
            PluginManager pluginManager,
            @Value("${spring.flyway.table:schema_histories}") String schemaHistoryTable) {
        return args -> {
            List<FlywayMigrationExtension> extensions = pluginManager.getExtensions(FlywayMigrationExtension.class);

            Set<String> locations = new LinkedHashSet<>();
            locations.add(PLATFORM_MIGRATION_LOCATION);

            ClassLoader compositeClassLoader = buildCompositeClassLoader(pluginManager, extensions, locations);

            LOG.info("[Flyway] Running migrations from {} location(s): {}", locations.size(), locations);

            Flyway flyway = Flyway.configure(compositeClassLoader)
                    .dataSource(dataSource)
                    .locations(locations.toArray(new String[0]))
                    .table(schemaHistoryTable)
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

        for (PluginWrapper wrapper : pluginManager.getStartedPlugins()) {
            ClassLoader pluginCl = pluginManager.getPluginClassLoader(wrapper.getPluginId());
            if (pluginCl instanceof URLClassLoader urlCl) {
                for (URL url : urlCl.getURLs()) {
                    urls.add(url);
                    locations.addAll(discoverMigrationLocations(url));
                    LOG.debug("[Flyway] Added classpath URL from plugin '{}': {}", wrapper.getPluginId(), url);
                }
            }
        }

        for (FlywayMigrationExtension ext : extensions) {
            String pluginId = resolvePluginId(pluginManager, ext);
            String location = ext.getMigrationLocation();
            locations.add(location);
            LOG.info("[Flyway] Plugin '{}' registered -> location: {}", pluginId, location);
        }

        return urls.isEmpty() ? parent
                : new URLClassLoader(urls.toArray(new URL[0]), parent);
    }

    private Set<String> discoverMigrationLocations(URL pluginUrl) {
        Set<String> locations = new LinkedHashSet<>();
        if (!"file".equals(pluginUrl.getProtocol()) || !pluginUrl.getPath().endsWith(".jar")) {
            return locations;
        }

        try (JarFile jar = new JarFile(Paths.get(pluginUrl.toURI()).toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith("db/migration/") || !name.endsWith(".sql")) {
                    continue;
                }

                String relative = name.substring("db/migration/".length());
                int slash = relative.indexOf('/');
                if (slash > 0) {
                    locations.add("classpath:db/migration/" + relative.substring(0, slash));
                }
            }
        } catch (Exception ex) {
            LOG.warn("[Flyway] Could not inspect plugin jar for migrations: {}", pluginUrl, ex);
        }
        return locations;
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
