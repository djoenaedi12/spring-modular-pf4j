package gasi.gps.platform.bootstrap;

import java.nio.file.Path;
import java.util.List;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.core.io.DefaultResourceLoader;

import gasi.gps.platform.infrastructure.classloader.CompositeClassLoader;

/**
 * Boots PF4J before Spring creates the application context.
 */
public class PluginBootstrap {

    private final Path pluginsPath;

    /**
     * Creates a plugin bootstrapper.
     *
     * @param pluginsPath directory containing plugin JAR files
     */
    public PluginBootstrap(Path pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    /**
     * Loads plugins, starts them, and returns the runtime objects needed by
     * Spring Boot.
     *
     * @param appClassLoader current application classloader
     * @return plugin runtime
     */
    public PluginRuntime bootstrap(ClassLoader appClassLoader) {
        DefaultPluginManager pluginManager = new DefaultPluginManager(pluginsPath);
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<ClassLoader> pluginClassLoaders = pluginManager.getStartedPlugins().stream()
                .map(plugin -> plugin.getPluginClassLoader())
                .toList();

        CompositeClassLoader classLoader = new CompositeClassLoader(appClassLoader, pluginClassLoaders);
        Thread.currentThread().setContextClassLoader(classLoader);

        return new PluginRuntime(pluginManager, classLoader);
    }

    /**
     * Creates a resource loader backed by the plugin runtime classloader.
     *
     * @param runtime plugin runtime
     * @return resource loader
     */
    public DefaultResourceLoader resourceLoader(PluginRuntime runtime) {
        return new DefaultResourceLoader(runtime.classLoader());
    }

    /**
     * Registers the plugin manager as a Spring singleton bean.
     *
     * @param app     Spring application
     * @param runtime plugin runtime
     */
    public void registerPluginManager(org.springframework.boot.SpringApplication app, PluginRuntime runtime) {
        PluginManager pluginManager = runtime.pluginManager();
        app.addInitializers(ctx -> ctx.getBeanFactory()
                .registerSingleton("pluginManager", pluginManager));
    }
}
