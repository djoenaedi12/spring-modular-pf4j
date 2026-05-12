package gasi.gps.core.api.i18n;

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * Extension point for registering plugin message bundle basenames.
 *
 * <p>
 * The host application merges all basenames provided by plugins into its
 * message source, allowing each plugin to define its own message keys and
 * optionally override shared messages.
 * </p>
 *
 * @see org.pf4j.ExtensionPoint
 * @since 1.0.0
 */
public interface I18nExtension extends ExtensionPoint {

    /**
     * Returns the list of message bundle basenames owned by this plugin.
     *
     * <p>
     * Prefer module-specific paths, for example
     * {@code List.of("classpath:i18n/auth/messages")}, when plugins may be
     * packaged together on one classpath.
     * </p>
     *
     * <p>
     * Expected files on the plugin classpath:
     * </p>
     * <ul>
     * <li>{@code i18n/auth/messages.properties} &mdash; default / EN</li>
     * <li>{@code i18n/auth/messages_id.properties} &mdash; Bahasa Indonesia</li>
     * </ul>
     *
     * @return message bundle basenames
     */
    List<String> getMessageBasenames();
}
