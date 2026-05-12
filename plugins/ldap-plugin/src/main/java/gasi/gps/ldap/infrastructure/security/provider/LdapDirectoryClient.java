package gasi.gps.ldap.infrastructure.security.provider;

import java.util.Hashtable;
import java.util.Locale;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.stereotype.Component;

import gasi.gps.core.api.security.AuthProviderException;
import gasi.gps.core.api.security.AuthProviderUserNotFoundException;
import gasi.gps.ldap.application.service.LdapConfigService;

/**
 * Low-level LDAP directory operations.
 */
@Component
public class LdapDirectoryClient {

    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * Resolves a username to its LDAP distinguished name.
     *
     * @param config LDAP runtime config
     * @param username login username
     * @return resolved user distinguished name
     */
    public String resolveUserDn(LdapConfigService.ActiveLdapConfig config, String username) {
        DirContext serviceContext = null;
        try {
            serviceContext = openContext(config, config.bindDn(), config.bindPassword());
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[0]);

            NamingEnumeration<SearchResult> results = serviceContext.search(
                    config.baseDn(),
                    config.userSearchFilter(),
                    new Object[] { username },
                    controls);

            if (!results.hasMore()) {
                throw new AuthProviderUserNotFoundException(
                        "User not found in LDAP: " + username);
            }

            SearchResult result = results.next();
            return extractDistinguishedName(config, result);
        } catch (AuthProviderUserNotFoundException ex) {
            throw ex;
        } catch (NamingException ex) {
            throw new AuthProviderException("LDAP user lookup failed", ex);
        } finally {
            closeQuietly(serviceContext);
        }
    }

    /**
     * Validates user credentials by binding to LDAP as the resolved user DN.
     *
     * @param config LDAP runtime config
     * @param userDn resolved user distinguished name
     * @param password user password
     */
    public void bindAsUser(LdapConfigService.ActiveLdapConfig config, String userDn, String password) {
        DirContext ctx = null;
        try {
            ctx = openContext(config, userDn, password);
        } catch (AuthenticationException ex) {
            throw new AuthProviderException("Invalid LDAP credentials", ex);
        } catch (NamingException ex) {
            throw new AuthProviderException("LDAP authentication failed", ex);
        } finally {
            closeQuietly(ctx);
        }
    }

    private DirContext openContext(LdapConfigService.ActiveLdapConfig config, String principal, String credentials)
            throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, config.ldapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(config.connectTimeoutMs()));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(config.readTimeoutMs()));
        return new InitialDirContext(env);
    }

    private String extractDistinguishedName(LdapConfigService.ActiveLdapConfig config, SearchResult result)
            throws NamingException {
        String name;
        try {
            name = result.getNameInNamespace();
        } catch (UnsupportedOperationException ex) {
            name = result.getName();
        }

        if (isBlank(name)) {
            throw new NamingException("LDAP search result did not include a DN");
        }

        String lowerName = name.toLowerCase(Locale.ROOT);
        String lowerBaseDn = config.baseDn().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(lowerBaseDn)) {
            return name;
        }
        return name + "," + config.baseDn();
    }

    private void closeQuietly(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException ignored) {
                // best-effort close
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
