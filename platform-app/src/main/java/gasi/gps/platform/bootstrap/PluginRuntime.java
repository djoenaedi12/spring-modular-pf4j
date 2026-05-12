package gasi.gps.platform.bootstrap;

import org.pf4j.PluginManager;

import gasi.gps.platform.infrastructure.classloader.CompositeClassLoader;

/**
 * Runtime objects created during plugin bootstrap.
 *
 * @param pluginManager the PF4J plugin manager
 * @param classLoader   the composite application and plugin classloader
 */
public record PluginRuntime(
        PluginManager pluginManager,
        CompositeClassLoader classLoader) {
}
