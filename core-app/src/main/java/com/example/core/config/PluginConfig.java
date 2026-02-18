package com.example.core.config;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Paths;

@Configuration
public class PluginConfig {

    @Bean
    public SpringPluginManager pluginManager() {
        // Secara default akan mencari folder "plugins" di root project
        // Anda bisa custom lokasinya di sini jika mau
        return new SpringPluginManager(Paths.get("plugins"));
    }
}
