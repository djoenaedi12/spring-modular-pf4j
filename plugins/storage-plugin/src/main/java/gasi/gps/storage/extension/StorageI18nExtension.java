package gasi.gps.storage.extension;

import java.util.List;
import org.pf4j.Extension;

import gasi.gps.core.api.i18n.I18nExtension;

/**
 * Registers the i18n basename file for plugin storage-plugin.
 *
 * <p>Message file: {@code src/main/resources/classpath:i18n/storage/messages_{locale}.properties}.</p>
 */
@Extension
public class StorageI18nExtension implements I18nExtension {

    @Override
    public List<String> getMessageBasenames() {
        return List.of("classpath:i18n/storage/messages");
    }
}
