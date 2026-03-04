package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.application.dto.RefreshTokenRequest;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.core.api.presentation.dto.ApiResponse;
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

    /**
     * Constructs AuthController.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate a user and return JWT tokens.
     *
     * @param request login credentials
     * @return JWT access and refresh tokens
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(
                request.getUsername(), request.getPassword());
        return ApiResponse.ok(response);
    }

    /**
     * Refresh an access token.
     *
     * @param request containing the refresh token
     * @return new JWT tokens
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(
                request.getRefreshToken());
        return ApiResponse.ok(response);
    }
}
