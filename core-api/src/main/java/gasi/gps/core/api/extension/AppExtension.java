package gasi.gps.core.api.extension;

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * Core extension point that every plugin module must implement.
 *
 * <p>
 * Provides basic metadata about the plugin to the core application,
 * allowing dynamic discovery and registration of modules at runtime
 * through the PF4J plugin framework.
 * </p>
 *
 * @see org.pf4j.ExtensionPoint
 */
public interface AppExtension extends ExtensionPoint {

    /**
     * Returns the unique name of this plugin module.
     *
     * @return the module name, e.g. {@code "inventory"} or {@code "order"}
     */
    String getModuleName();

    /**
     * Returns a human-readable description of this plugin module.
     *
     * @return the module description
     */
    String getModuleDescription();

    /**
     * Returns the version of this plugin module.
     *
     * @return the module version, e.g. {@code "1.0.0"}
     */
    String getModuleVersion();

    /**
     * Returns Spring base packages owned by this plugin module.
     *
     * <p>
     * Platform hosts can use this metadata for diagnostics or future dynamic
     * registration flows.
     * </p>
     *
     * @return base packages provided by this plugin, e.g.
     *         {@code List.of("gasi.gps.auth")}
     */
    List<String> getBasePackages();
}
