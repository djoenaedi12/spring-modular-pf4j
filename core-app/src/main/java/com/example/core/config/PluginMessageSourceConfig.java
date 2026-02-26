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
 * Configuration that builds a composite {@link MessageSource} by merging
 * message bundles from all loaded plugins with the core application messages.
 *
 * <p>
 * <strong>Message resolution order:</strong>
 * </p>
 * <ol>
 * <li>Plugin-specific message bundles (registered via
 * {@link I18nExtension})</li>
 * <li>Core shared messages ({@code messages/core.properties}) as fallback</li>
 * </ol>
 *
 * <p>
 * This allows plugins to define their own message keys and optionally
 * override shared keys from the core application.
 * </p>
 *
 * @see I18nExtension
 * @since 1.0.0
 */
@Configuration
public class PluginMessageSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(PluginMessageSourceConfig.class);

    /**
     * Creates a {@link ReloadableResourceBundleMessageSource} that combines
     * message basenames from all plugins with the core application's basenames.
     *
     * @param pluginManager the PF4J plugin manager used to discover
     *                      {@link I18nExtension} implementations
     * @return the composite {@link MessageSource}
     */
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
