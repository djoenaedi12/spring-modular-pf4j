package gasi.gps.platform.presentation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.PluginManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.platform.bootstrap.PluginMetadataRegistry;

/**
 * REST controller that exposes platform application endpoints.
 *
 * <p>
 * Provides a health-check endpoint that reports the status of the
 * platform application along with information about all currently loaded
 * and started PF4J plugins.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/manage")
public class PlatformController {

    private final PluginManager pluginManager;
    private final PluginMetadataRegistry pluginMetadataRegistry;

    /**
     * Constructs a new {@code PlatformController}.
     *
     * @param pluginManager          the PF4J plugin manager used to query loaded
     *                               plugins
     * @param pluginMetadataRegistry plugin metadata registry
     */
    public PlatformController(PluginManager pluginManager,
            PluginMetadataRegistry pluginMetadataRegistry) {
        this.pluginManager = pluginManager;
        this.pluginMetadataRegistry = pluginMetadataRegistry;
    }

    /**
     * Returns the health status of the platform application, including a list
     * of all started plugins with their ID, version, and current state.
     *
     * <p>
     * Example response:
     * </p>
     *
     * <pre>{@code
     * {
     * "status": "UP",
     * "app": "platform-app",
     * "plugins": [
     * { "id": "inventory-plugin", "version": "1.0.0", "state": "STARTED" }
     * ],
     * "modules": [
     * { "name": "inventory-plugin", "basePackages": ["gasi.gps.inventory"] }
     * ]
     * }
     * }</pre>
     *
     * @return a map containing the application status and plugin details
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("app", "platform-app");

        List<Map<String, String>> plugins = pluginManager.getStartedPlugins().stream()
                .map(p -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("id", p.getPluginId());
                    info.put("version", p.getDescriptor().getVersion());
                    info.put("state", p.getPluginState().toString());
                    return info;
                })
                .toList();

        result.put("plugins", plugins);
        result.put("modules", pluginMetadataRegistry.modules());
        result.put("pluginBasePackages", pluginMetadataRegistry.basePackages());
        return result;
    }
}
