package gasi.gps.platform;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import gasi.gps.platform.bootstrap.PluginBootstrap;
import gasi.gps.platform.bootstrap.PluginRuntime;
import gasi.gps.platform.bootstrap.PlatformScanPackages;

/**
 * Main entry point for the modular Spring Boot application.
 *
 * <p>
 * This class delegates PF4J startup to {@link PluginBootstrap} before Spring
 * Boot starts. The resulting plugin runtime exposes a composite classloader and
 * a singleton {@code PluginManager} bean for the application context.
 * </p>
 *
 * <p>
 * <strong>Startup sequence:</strong>
 * </p>
 * <ol>
 * <li>Load and start all PF4J plugins from the {@code plugins/} directory</li>
 * <li>Build a composite classloader combining app + plugin classloaders</li>
 * <li>Launch Spring Boot with the composite classloader as the context
 * classloader</li>
 * </ol>
 *
 * @see PluginBootstrap
 * @since 1.0.0
 */
@EntityScan(basePackages = PlatformScanPackages.PLUGIN_ROOT)
@EnableJpaRepositories(basePackages = PlatformScanPackages.PLUGIN_ROOT)
@SpringBootApplication(scanBasePackages = {
        PlatformScanPackages.PLATFORM,
        PlatformScanPackages.CORE_STARTER
})
public class PlatformApplication {

    private static final Path DEFAULT_PLUGINS_PATH = Paths.get("plugins");

    /**
     * Application entry point that bootstraps PF4J plugins before Spring Boot.
     *
     * @param args command-line arguments passed to the Spring Boot application
     */
    public static void main(String[] args) {
        PluginBootstrap bootstrap = new PluginBootstrap(DEFAULT_PLUGINS_PATH);
        PluginRuntime runtime = bootstrap.bootstrap(Thread.currentThread().getContextClassLoader());

        SpringApplication app = new SpringApplication(
                bootstrap.resourceLoader(runtime),
                PlatformApplication.class);

        bootstrap.registerPluginManager(app, runtime);
        app.run(args);
    }
}
