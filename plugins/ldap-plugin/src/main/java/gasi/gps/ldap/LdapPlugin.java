package gasi.gps.ldap;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PF4J plugin class for LDAP authentication provider module.
 */
public class LdapPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(LdapPlugin.class);

    @Override
    public void start() {
        LOG.info("LDAP plugin started");
    }

    @Override
    public void stop() {
        LOG.info("LDAP plugin stopped");
    }
}
