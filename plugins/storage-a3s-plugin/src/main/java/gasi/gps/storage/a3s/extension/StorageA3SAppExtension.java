package gasi.gps.storage.a3s.extension;

import java.util.List;
import org.pf4j.Extension;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Registers the basic information of plugin storage-a3s-plugin to the core application.
 */
@Extension
public class StorageA3SAppExtension implements AppExtension {

    @Override
    public String getModuleName() {
        return "storage-a3s-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "S3-compatible storage provider plugin";
    }

    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }

    @Override
    public List<String> getBasePackages() {
        return List.of("gasi.gps.storage.a3s");
    }
}
