package gasi.gps.media;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for storage-plugin.
 *
 * <p>
 * Loaded by PF4J during platform-app startup. The plugin's Spring beans
 * are discovered by the platform host through the composite classloader and package scanning.
 * </p>
 */
public class StoragePlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(StoragePlugin.class);

    @Override
    public void start() {
        super.start();
        LOG.info("{} started", "storage-plugin");
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", "storage-plugin");
        super.stop();
    }
}
