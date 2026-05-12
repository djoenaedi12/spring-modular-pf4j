package gasi.gps.audit;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PF4J plugin class for the Audit module.
 *
 * <p>
 * This plugin provides automatic audit logging for CUD operations
 * (via {@code @AuditableEntity}) and custom method-level auditing
 * (via {@code @Auditable}).
 * </p>
 */
public class AuditPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(AuditPlugin.class);

    @Override
    public void start() {
        LOG.info("Audit plugin started");
    }

    @Override
    public void stop() {
        LOG.info("Audit plugin stopped");
    }
}
