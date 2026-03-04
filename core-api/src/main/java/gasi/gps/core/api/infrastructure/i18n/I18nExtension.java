package gasi.gps.core.api.infrastructure.i18n;

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for registering i18n message basenames from a plugin.
 *
 * <p>
 * The core application merges all basenames provided by plugins into a
 * single {@code CompositeMessageSource}, allowing each plugin to define its
 * own message keys and optionally override shared messages from the core.
 * </p>
 *
 * @see org.pf4j.ExtensionPoint
 * @see gasi.gps.core.config.PluginMessageSourceConfig
 * @since 1.0.0
 */
public interface I18nExtension extends ExtensionPoint {

    /**
     * Returns the list of message bundle basenames owned by this plugin.
     *
     * <p>
     * Konvensi: gunakan {@code List.of("classpath:messages/messages")} agar seragam
     * antar plugin.
     * Setiap plugin memiliki classloader sendiri sehingga tidak ada konflik nama.
     * </p>
     *
     * <p>
     * Expected files on the plugin classpath:
     * </p>
     * <ul>
     * <li>{@code messages/messages.properties} &mdash; default / EN</li>
     * <li>{@code messages/messages_id.properties} &mdash; Bahasa Indonesia</li>
     * </ul>
     *
     * @return an unmodifiable list of basename strings
     */
    List<String> getMessageBasenames();
}
