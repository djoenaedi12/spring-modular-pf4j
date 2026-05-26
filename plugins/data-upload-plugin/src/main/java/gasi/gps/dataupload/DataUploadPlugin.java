package gasi.gps.dataupload;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for data-upload-plugin.
 *
 * <p>
 * Loaded by PF4J during platform-app startup. The plugin's Spring beans
 * are discovered by the platform host through the composite classloader and package scanning.
 * </p>
 */
public class DataUploadPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(DataUploadPlugin.class);

    @Override
    public void start() {
        super.start();
        LOG.info("{} started", "data-upload-plugin");
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", "data-upload-plugin");
        super.stop();
    }
}
