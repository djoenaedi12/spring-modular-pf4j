package {{FULL_PACKAGE}};

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for {{PLUGIN_ID}}.
 *
 * <p>
 * Loaded by PF4J during platform-app startup. The plugin's Spring beans
 * are discovered by the platform host through the composite classloader and package scanning.
 * </p>
 */
public class {{PLUGIN_CLASS_NAME}} extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger({{PLUGIN_CLASS_NAME}}.class);

    @Override
    public void start() {
        super.start();
        LOG.info("{} started", "{{PLUGIN_ID}}");
    }

    @Override
    public void stop() {
        LOG.info("{} stopped", "{{PLUGIN_ID}}");
        super.stop();
    }
}
