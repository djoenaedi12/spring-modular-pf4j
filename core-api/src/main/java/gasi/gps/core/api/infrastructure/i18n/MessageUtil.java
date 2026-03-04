package gasi.gps.core.api.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for retrieving localized messages from the Spring
 * {@link MessageSource}.
 *
 * <p>
 * This component provides convenient methods to fetch messages based on the
 * current locale, with support for message arguments and default values.
 * </p>
 *
 * <p>
 * It is designed to be injected into services or controllers that need to
 * access localized messages, abstracting away direct interaction with the
 * MessageSource and locale management.
 * </p>
 *
 * @see MessageSource
 * @since 1.0.0
 */
@Component
public class MessageUtil {

    private final MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get message with current locale
     */
    public String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message with arguments
     * Usage: messageUtil.get("gps.payroll.error.cutoff", "25")
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message with fallback
     */
    public String getOrDefault(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }
}
