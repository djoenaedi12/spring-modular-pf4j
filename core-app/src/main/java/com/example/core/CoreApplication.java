package com.example.core;

import com.example.core.classloader.CompositeClassLoader;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the modular Spring Boot application.
 *
 * <p>
 * This class bootstraps the PF4J plugin framework <strong>before</strong>
 * Spring Boot starts, creating a {@link CompositeClassLoader} that merges the
 * application classloader with all plugin classloaders. This allows Spring's
 * component scan, JPA entity scan, and repository scan to discover classes
 * from dynamically loaded plugins.
 * </p>
 *
 * <p>
 * <strong>Startup sequence:</strong>
 * </p>
 * <ol>
 * <li>Load and start all PF4J plugins from the {@code plugins/} directory</li>
 * <li>Build a {@link CompositeClassLoader} combining app + plugin
 * classloaders</li>
 * <li>Launch Spring Boot with the composite classloader as the context
 * classloader</li>
 * </ol>
 *
 * @see CompositeClassLoader
 * @see org.pf4j.DefaultPluginManager
 * @since 1.0.0
 */
@EntityScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
@SpringBootApplication(scanBasePackages = "com.example")
public class CoreApplication {

    /**
     * Application entry point that bootstraps PF4J plugins before Spring Boot.
     *
     * @param args command-line arguments passed to the Spring Boot application
     */
    public static void main(String[] args) {
        // Step 1: Load and start plugins before Spring context initialization
        DefaultPluginManager pluginManager = new DefaultPluginManager(Paths.get("plugins"));
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        // Step 2: Build a composite classloader (app + all plugin classloaders)
        // so that Spring component scan can discover plugin classes
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        List<ClassLoader> pluginClassLoaders = new ArrayList<>();
        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            pluginClassLoaders.add(plugin.getPluginClassLoader());
        }
        CompositeClassLoader compositeClassLoader = new CompositeClassLoader(appClassLoader, pluginClassLoaders);

        // Set as context classloader for all frameworks (Spring, Hibernate, etc.)
        Thread.currentThread().setContextClassLoader(compositeClassLoader);

        // Step 3: Start Spring Boot with the composite classloader
        SpringApplication app = new SpringApplication(
                new DefaultResourceLoader(compositeClassLoader),
                CoreApplication.class);

        // Register PluginManager as a singleton bean for dependency injection
        app.addInitializers(ctx -> {
            ctx.getBeanFactory().registerSingleton("pluginManager", pluginManager);
        });

        app.run(args);
    }

    /**
     * Prints a startup summary to the console listing all loaded plugins
     * and their status, executed automatically after the Spring context is ready.
     *
     * @param pluginManager the PF4J plugin manager
     * @return a {@link CommandLineRunner} that logs plugin information
     */
    @Bean
    public CommandLineRunner checkPlugins(PluginManager pluginManager) {
        return args -> {
            System.out.println("");
            System.out.println("==============================================");
            System.out.println("   MODULAR SYSTEM STARTUP CHECK               ");
            System.out.println("==============================================");

            List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();

            if (startedPlugins.isEmpty()) {
                System.out.println("STATUS: Tidak ada plugin yang aktif.");
                System.out.println("INFO  : Pastikan JAR ada di folder /plugins");
            } else {
                System.out.println("STATUS: Berhasil memuat " + startedPlugins.size() + " plugin.");
                for (PluginWrapper plugin : startedPlugins) {
                    System.out.println("----------------------------------------------");
                    System.out.println("ID Plugin      : " + plugin.getPluginId());
                    System.out.println("Versi          : " + plugin.getDescriptor().getVersion());
                    System.out.println("Class Utama    : " + plugin.getDescriptor().getPluginClass());
                    System.out.println("Status         : " + plugin.getPluginState());
                }
            }
            System.out.println("==============================================");
            System.out.println("");
        };
    }
}