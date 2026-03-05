package gasi.gps.auth.infrastructure.security.provider;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import gasi.gps.core.api.infrastructure.security.AuthProviderCredentials;
import gasi.gps.core.api.infrastructure.security.AuthProviderExtension;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;

/**
 * Default local authentication provider backed by application database users.
 */
@Component
public class LocalAuthProvider implements AuthProviderExtension {

    public static final String PROVIDER_ID = "local";

    private final AuthenticationManager authenticationManager;

    /**
     * Constructs LocalAuthProvider.
     *
     * @param authenticationManager Spring Security authentication manager
     */
    public LocalAuthProvider(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticatedPrincipal authenticate(AuthProviderCredentials credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.principal(), credentials.secret()));

        return new AuthenticatedPrincipal(
                PROVIDER_ID,
                null,
                authentication.getName(),
                Set.of(),
                credentials.attributes() != null ? credentials.attributes() : java.util.Map.of());
    }
}
