package gasi.gps.auth.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.ForgotPasswordRequest;
import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.application.dto.ResetPasswordRequest;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.auth.infrastructure.security.JwtUtil;
import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.infrastructure.security.SecurityContextUtil;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for authentication endpoints.
 *
 * <p>
 * Login, forgot-password, and reset-password are publicly accessible.
 * Logout requires a valid JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final String DEVICE_MODEL_HEADER = "X-Device-Model";
    private static final String FORGOT_PASSWORD_MESSAGE = "If the account exists, a password reset link has been sent";
    private static final String RESET_PASSWORD_MESSAGE = "Password has been reset successfully";
    private static final String LOGOUT_MESSAGE = "Logged out successfully";

    private final AuthService authService;
    private final SecurityContextUtil securityContextUtil;
    private final JwtUtil jwtUtil;

    /**
     * Constructs AuthController.
     */
    public AuthController(AuthService authService, SecurityContextUtil securityContextUtil, JwtUtil jwtUtil) {
        this.authService = authService;
        this.securityContextUtil = securityContextUtil;
        this.jwtUtil = jwtUtil;
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

        request.setDeviceId(httpServletRequest.getHeader(DEVICE_ID_HEADER));
        request.setDeviceModel(httpServletRequest.getHeader(DEVICE_MODEL_HEADER));
        request.setIpAddress(securityContextUtil.getCurrentIp());
        request.setUserAgent(securityContextUtil.getCurrentUserAgent());

        if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);

            if (values.length == 2) {
                String clientId = values[0];
                String clientSecret = values[1];
                return ApiResponse.ok(authService.login(clientId, clientSecret, request));
            }
        }

        // Fallback for non-client authentication flow
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    /**
     * Initiates forgot-password flow.
     */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null, FORGOT_PASSWORD_MESSAGE);
    }

    /**
     * Resets password using reset token.
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null, RESET_PASSWORD_MESSAGE);
    }

    /**
     * Logs out by revoking the current access token session.
     * Requires a valid JWT in the Authorization header.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("Bearer token is required");
        }

        String token = header.substring(BEARER_PREFIX.length());
        String jti = jwtUtil.getJtiFromToken(token);
        authService.logout(jti);
        return ApiResponse.ok(null, LOGOUT_MESSAGE);
    }
}
