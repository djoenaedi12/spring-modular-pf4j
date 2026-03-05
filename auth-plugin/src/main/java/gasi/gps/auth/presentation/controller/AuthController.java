package gasi.gps.auth.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.infrastructure.security.SecurityContextUtil;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for authentication endpoints.
 *
 * <p>
 * All endpoints are publicly accessible (whitelisted in SecurityConfig).
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextUtil securityContextUtil;

    /**
     * Constructs AuthController.
     */
    public AuthController(AuthService authService, SecurityContextUtil securityContextUtil) {
        this.authService = authService;
        this.securityContextUtil = securityContextUtil;
    }

    /**
     * Authenticate a user and return JWT tokens.
     * Optionally supports Basic Auth for client authentication.
     *
     * @param authHeader HTTP Authorization header
     * @param request    login credentials
     * @return JWT access and refresh tokens
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            HttpServletRequest httpServletRequest,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody LoginRequest request) {

        request.setIpAddress(securityContextUtil.getCurrentIp());
        request.setUserAgent(securityContextUtil.getCurrentUserAgent());

        if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);

                if (values.length == 2) {
                    String clientId = values[0];
                    String clientSecret = values[1];
                    return ApiResponse.ok(authService.login(clientId, clientSecret, request));
                }
            } catch (Exception e) {
                throw new BusinessException("Invalid Basic Authentication format");
            }
        }

        // Fallback for non-client authentication flow
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }
}
