package gasi.gps.platform.bootstrap;

import java.util.List;

import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Reads plugin metadata exposed through {@link AppExtension}.
 */
@Component
public class PluginMetadataRegistry {

    private final PluginManager pluginManager;

    /**
     * Creates a plugin metadata registry.
     *
     * @param pluginManager PF4J plugin manager
     */
    public PluginMetadataRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * Returns metadata for all plugin app extensions.
     *
     * @return plugin module metadata
     */
    public List<PluginModuleMetadata> modules() {
        return pluginManager.getExtensions(AppExtension.class).stream()
                .map(extension -> new PluginModuleMetadata(
                        extension.getModuleName(),
                        extension.getModuleDescription(),
                        extension.getModuleVersion(),
                        extension.getBasePackages()))
                .toList();
    }

    /**
     * Returns all Spring base packages declared by plugins.
     *
     * @return plugin base packages
     */
    public List<String> basePackages() {
        return modules().stream()
                .flatMap(module -> module.basePackages().stream())
                .distinct()
                .toList();
    }

    /**
     * Plugin module metadata exposed by {@link AppExtension}.
     *
     * @param name         module name
     * @param description  module description
     * @param version      module version
     * @param basePackages Spring base packages owned by the module
     */
    public record PluginModuleMetadata(
            String name,
            String description,
            String version,
            List<String> basePackages) {
    }
}
