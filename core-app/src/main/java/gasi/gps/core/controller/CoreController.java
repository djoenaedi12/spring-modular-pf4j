package gasi.gps.core.controller;

import org.pf4j.PluginManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes core application endpoints.
 *
 * <p>
 * Provides a health-check endpoint that reports the status of the
 * core application along with information about all currently loaded
 * and started PF4J plugins.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class CoreController {

    private final PluginManager pluginManager;

    /**
     * Constructs a new {@code CoreController}.
     *
     * @param pluginManager the PF4J plugin manager used to query loaded plugins
     */
    public CoreController(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * Returns the health status of the core application, including a list
     * of all started plugins with their ID, version, and current state.
     *
     * <p>
     * Example response:
     * </p>
     *
     * <pre>{@code
     * {
     *   "status": "UP",
     *   "app": "core-app",
     *   "plugins": [
     *     { "id": "inventory-plugin", "version": "1.0.0", "state": "STARTED" }
     *   ]
     * }
     * }</pre>
     *
     * @return a map containing the application status and plugin details
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("app", "core-app");

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
        return result;
    }
}
