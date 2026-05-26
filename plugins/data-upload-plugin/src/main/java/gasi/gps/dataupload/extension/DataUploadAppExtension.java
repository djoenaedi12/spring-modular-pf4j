package gasi.gps.dataupload.extension;

import java.util.List;
import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Registers the basic information of plugin data-upload-plugin to the core application.
 */
@Extension
public class DataUploadAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "data-upload-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Data-upload plugin";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public List<String> getBasePackages() {
        return List.of("gasi.gps.dataupload");
    }
}
