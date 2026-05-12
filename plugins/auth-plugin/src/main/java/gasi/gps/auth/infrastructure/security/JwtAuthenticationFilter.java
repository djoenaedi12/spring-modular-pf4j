package gasi.gps.auth.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import gasi.gps.auth.domain.port.inbound.UserSessionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter that extracts the token from the Authorization
 * header, validates it against both signature and database session,
 * and populates the SecurityContext.
 *
 * <p>If the Bearer token is present but invalid or expired, immediately
 * responds with 401 via {@link RestAuthEntryPoint}.</p>
 */
@Order(20)
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserSessionService userSessionService;
    private final RestAuthEntryPoint restAuthEntryPoint;

    /**
     * Constructs the filter.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            UserSessionService userSessionService,
            RestAuthEntryPoint restAuthEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.userSessionService = userSessionService;
        this.restAuthEntryPoint = restAuthEntryPoint;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtUtil.getClaims(token);
            String jti = claims.getId();
            if (jti != null && userSessionService.validateAccessToken(jti)) {
                String externalId = claims.getSubject();
                String username = claims.get("username", String.class);
                List<String> roles = claims.get("roles", List.class);
                authenticateContext(username, roles, externalId);
                filterChain.doFilter(request, response);
            } else {
                rejectUnauthorized(request, response);
            }
        } catch (JwtException | IllegalArgumentException e) {
            rejectUnauthorized(request, response);
        }
    }

    private void rejectUnauthorized(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            restAuthEntryPoint.commence(request, response, null);
        } catch (AuthenticationException ex) {
            // should not happen since restAuthEntryPoint does not throw
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
