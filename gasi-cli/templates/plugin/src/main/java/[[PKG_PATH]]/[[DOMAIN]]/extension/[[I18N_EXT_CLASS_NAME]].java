package {{FULL_PACKAGE}}.extension;

import java.util.List;

import org.pf4j.Extension;

import gasi.gps.core.api.i18n.I18nExtension;

/**
 * Registers the i18n basename file for plugin {{PLUGIN_ID}}.
 *
 * <p>Message file: {@code src/main/resources/{{I18N_BASENAME}}_{locale}.properties}.</p>
 */
@Extension
public class {{I18N_EXT_CLASS_NAME}} implements I18nExtension {

    @Override
    public List<String> getMessageBasenames() {
        return List.of("{{I18N_BASENAME}}");
    }
}
