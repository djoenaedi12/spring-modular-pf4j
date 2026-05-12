package gasi.gps.core.starter.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring-backed helper for resolving localized messages.
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

    /**
     * Creates a message helper.
     *
     * @param messageSource Spring message source
     */
    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolves a message using the current locale.
     *
     * @param key message key
     * @return resolved message
     */
    public String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Resolves a parameterized message using the current locale.
     *
     * @param key  message key
     * @param args message arguments
     * @return resolved message
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Resolves a message with a fallback value.
     *
     * @param key            message key
     * @param defaultMessage fallback message when the key is missing
     * @return resolved message or fallback
     */
    public String getOrDefault(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }
}
