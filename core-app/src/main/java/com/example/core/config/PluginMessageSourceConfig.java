package com.example.core.config;

import com.example.api.I18nExtension;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Membuat MessageSource gabungan (shared core + tiap plugin).
 *
 * Urutan resolve pesan:
 * 1. Cari di message bundle plugin yang terdaftar (plugin-specific)
 * 2. Fallback ke shared messages di core-app (messages.properties)
 *
 * Dengan cara ini plugin bisa meng-override key shared jika diperlukan.
 */
@Configuration
public class PluginMessageSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(PluginMessageSourceConfig.class);

    @Bean
    public MessageSource messageSource(PluginManager pluginManager) {
        List<String> basenames = new ArrayList<>();

        // Ambil basenames dari semua plugin yang mengimplementasikan I18nExtension
        List<I18nExtension> extensions = pluginManager.getExtensions(I18nExtension.class);
        for (I18nExtension ext : extensions) {
            List<String> pluginBasenames = ext.getMessageBasenames();
            basenames.addAll(pluginBasenames);
            log.info("[i18n] Plugin message basenames registered: {}", pluginBasenames);
        }

        // Shared messages dari core selalu jadi fallback (ditambah terakhir)
        basenames.add("classpath:messages/core");

        log.info("[i18n] Total message basenames: {}", basenames);

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(basenames.toArray(new String[0]));
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true); // jika key tidak ada, tampilkan key-nya
        return messageSource;
    }
}
