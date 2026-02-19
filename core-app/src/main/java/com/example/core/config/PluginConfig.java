package com.example.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi terkait plugin.
 *
 * PluginManager sekarang dibuat di CoreApplication.main() SEBELUM Spring Boot
 * start, agar composite classloader bisa disiapkan terlebih dahulu.
 * PluginManager sudah di-register sebagai singleton bean di sana.
 */
@Configuration
public class PluginConfig {
    // Konfigurasi tambahan plugin bisa ditambahkan di sini
}
