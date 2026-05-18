package gasi.gps.storage.extension;

import java.util.List;
import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Registers the basic information of plugin storage-plugin to the core application.
 */
@Extension
public class StorageAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "storage-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Storage plugin";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public List<String> getBasePackages() {
        return List.of("gasi.gps.media");
    }
}
