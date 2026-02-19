package com.example.core.controller;

import org.pf4j.PluginManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CoreController {

    private final PluginManager pluginManager;

    public CoreController(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

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
