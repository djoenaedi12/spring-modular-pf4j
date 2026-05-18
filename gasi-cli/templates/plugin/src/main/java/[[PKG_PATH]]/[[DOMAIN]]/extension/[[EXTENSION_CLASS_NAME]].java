package {{FULL_PACKAGE}}.extension;

import java.util.List;

import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Registers the basic information of plugin {{PLUGIN_ID}} to the core application.
 */
@Extension
public class {{EXTENSION_CLASS_NAME}} implements AppExtension {

    @Override
    public String getModuleName() {
        return "{{PLUGIN_ID}}";
    }

    @Override
    public String getModuleDescription() {
        return "{{PLUGIN_DESCRIPTION}}";
    }

    @Override
    public String getModuleVersion() {
        return "{{PLUGIN_VERSION}}";
    }

    @Override
    public List<String> getBasePackages() {
        return List.of("{{FULL_PACKAGE}}");
    }
}
