package gasi.gps.ldap.infrastructure.security.provider;

import java.util.Map;
import java.util.Set;

import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.security.AuthProviderCredentials;
import gasi.gps.core.api.security.AuthProviderException;
import gasi.gps.core.api.security.AuthProviderExtension;
import gasi.gps.core.api.security.AuthenticatedPrincipal;
import gasi.gps.ldap.application.service.LdapConfigService;

/**
 * LDAP authentication provider implemented as a dedicated plugin.
 */
@Component
@Extension
public class LdapAuthProvider implements AuthProviderExtension {

    private static final String PROVIDER_ID = "ldap";

    private final LdapConfigService ldapConfigService;
    private final LdapDirectoryClient ldapDirectoryClient;

    /**
     * Constructs LDAP provider.
     *
     * @param ldapConfigService persisted LDAP config service
     * @param ldapDirectoryClient LDAP directory client
     */
    public LdapAuthProvider(LdapConfigService ldapConfigService, LdapDirectoryClient ldapDirectoryClient) {
        this.ldapConfigService = ldapConfigService;
        this.ldapDirectoryClient = ldapDirectoryClient;
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthProviderCredentials credentials) {
        if (credentials == null) {
            throw new AuthProviderException("Invalid LDAP credentials");
        }

        LdapConfigService.ActiveLdapConfig config = ldapConfigService.getActiveConfig();
        validateConfig(config);
        String principal = credentials.principal();
        String secret = credentials.secret();

        if (isBlank(principal) || isBlank(secret)) {
            throw new AuthProviderException("Invalid LDAP credentials");
        }

        String userDn = ldapDirectoryClient.resolveUserDn(config, principal);
        ldapDirectoryClient.bindAsUser(config, userDn, secret);

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
                            + "Set LDAP settings via /api/v1/ldap-config");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
