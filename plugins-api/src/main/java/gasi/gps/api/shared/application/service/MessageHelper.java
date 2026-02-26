package gasi.gps.api.shared.application.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper to resolve i18n messages from message keys.
 */
@Component
public class MessageHelper {

    private final MessageSource messageSource;

    public MessageHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolve a message key using the current locale.
     *
     * @param key  message key (e.g. "product.duplicate.name")
     * @param args placeholder values
     * @return resolved message
     */
    public String get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }
}
