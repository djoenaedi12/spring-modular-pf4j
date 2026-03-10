package gasi.gps.auth.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import gasi.gps.auth.domain.port.inbound.UserApiTokenService;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API Token authentication filter that extracts the token from the X-API-Key
 * header, validates it via UserApiTokenService, and populates the SecurityContext.
 *
 * <p>If the header is present but the key is invalid, immediately responds
 * with 401 via {@link RestAuthEntryPoint}.</p>
 */
@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final UserApiTokenService userApiTokenService;
    private final RestAuthEntryPoint restAuthEntryPoint;

    /**
     * Constructs the filter.
     */
    public ApiTokenAuthenticationFilter(UserApiTokenService userApiTokenService,
            RestAuthEntryPoint restAuthEntryPoint) {
        this.userApiTokenService = userApiTokenService;
        this.restAuthEntryPoint = restAuthEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AuthenticatedPrincipal> principal = userApiTokenService.authenticate(apiKey);
        if (principal.isPresent()) {
            AuthenticatedPrincipal p = principal.get();
            authenticateContext(
                    p.username(),
                    p.roles() != null ? p.roles().stream().toList() : List.of(),
                    p.externalId());
            filterChain.doFilter(request, response);
        } else {
            try {
                restAuthEntryPoint.commence(request, response, null);
            } catch (AuthenticationException ex) {
                // should not happen since restAuthEntryPoint does not throw
            }
        }
    }

    private void authenticateContext(String username, List<String> roles, String externalId) {
        List<SimpleGrantedAuthority> authorities = List.of();
        if (roles != null) {
            authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
        authToken.setDetails(externalId);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
