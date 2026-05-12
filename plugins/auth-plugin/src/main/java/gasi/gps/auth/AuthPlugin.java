package gasi.gps.auth;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PF4J plugin class for the Auth module.
 *
 * <p>
 * This plugin provides JWT-based authentication, user management,
 * and role-based access control (RBAC).
 * </p>
 */
public class AuthPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(AuthPlugin.class);

    @Override
    public void start() {
        LOG.info("Auth plugin started");
    }

    @Override
    public void stop() {
        LOG.info("Auth plugin stopped");
    }
}
