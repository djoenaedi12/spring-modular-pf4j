package gasi.gps.dataupload.extension;

import java.util.List;
import org.pf4j.Extension;

import gasi.gps.core.api.i18n.I18nExtension;

/**
 * Registers the i18n basename file for plugin data-upload-plugin.
 *
 * <p>Message file: {@code src/main/resources/classpath:i18n/data-upload/messages_{locale}.properties}.</p>
 */
@Extension
public class DataUploadI18nExtension implements I18nExtension {

    @Override
    public List<String> getMessageBasenames() {
        return List.of("classpath:i18n/data-upload/messages");
    }
}
