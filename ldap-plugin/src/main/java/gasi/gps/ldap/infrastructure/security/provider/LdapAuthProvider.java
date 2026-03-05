package gasi.gps.ldap.infrastructure.security.provider;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.infrastructure.security.AuthProviderCredentials;
import gasi.gps.core.api.infrastructure.security.AuthProviderException;
import gasi.gps.core.api.infrastructure.security.AuthProviderExtension;
import gasi.gps.core.api.infrastructure.security.AuthProviderUserNotFoundException;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;
import gasi.gps.ldap.application.service.LdapConfigService;

/**
 * LDAP authentication provider implemented as a dedicated plugin.
 */
@Component
@Extension
public class LdapAuthProvider implements AuthProviderExtension {

    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String PROVIDER_ID = "ldap";

    private final LdapConfigService ldapConfigService;

    /**
     * Constructs LDAP provider.
     *
     * @param ldapConfigService persisted LDAP config service
     */
    public LdapAuthProvider(LdapConfigService ldapConfigService) {
        this.ldapConfigService = ldapConfigService;
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthProviderCredentials credentials) {
        LdapConfigService.ActiveLdapConfig config = ldapConfigService.getActiveConfig();
        validateConfig(config);
        String principal = credentials.principal();
        String secret = credentials.secret();

        if (isBlank(principal) || isBlank(secret)) {
            throw new AuthProviderException("Invalid LDAP credentials");
        }

        String userDn = resolveUserDn(config, principal);
        bindAsUser(config, userDn, secret);

        return new AuthenticatedPrincipal(
                PROVIDER_ID,
                userDn,
                principal,
                Set.of(),
                credentials.attributes() != null ? credentials.attributes() : Map.of());
    }

    private void validateConfig(LdapConfigService.ActiveLdapConfig config) {
        if (!config.enabled()) {
            throw new AuthProviderException("LDAP provider is disabled");
        }

        if (isBlank(config.ldapUrl())
                || isBlank(config.baseDn())
                || isBlank(config.bindDn())
                || isBlank(config.bindPassword())
                || isBlank(config.userSearchFilter())) {
            throw new AuthProviderException(
                    "LDAP provider is not configured. "
                            + "Set LDAP settings via /api/ldap-config");
        }
    }

    private String resolveUserDn(LdapConfigService.ActiveLdapConfig config, String username) {
        try (DirContext serviceContext = openContext(config, config.bindDn(), config.bindPassword())) {
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
        }
    }

    private void bindAsUser(LdapConfigService.ActiveLdapConfig config, String userDn, String password) {
        try (DirContext ignored = openContext(config, userDn, password)) {
            // successful bind means authentication success
        } catch (AuthenticationException ex) {
            throw new AuthProviderException("Invalid LDAP credentials", ex);
        } catch (NamingException ex) {
            throw new AuthProviderException("LDAP authentication failed", ex);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
