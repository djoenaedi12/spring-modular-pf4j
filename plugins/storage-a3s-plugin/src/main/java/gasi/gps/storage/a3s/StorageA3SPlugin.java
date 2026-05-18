package gasi.gps.storage.a3s;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for storage-a3s-plugin.
 *
 * <p>
 * Loaded by PF4J during platform-app startup. The plugin's Spring beans
 * are discovered by the platform host through the composite classloader and package scanning.
 * </p>
 */
public class StorageA3SPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(StorageA3SPlugin.class);

    @Override
    public void start() {
        super.start();
        LOG.info("{} started", "storage-a3s-plugin");
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", "storage-a3s-plugin");
        super.stop();
    }
}
