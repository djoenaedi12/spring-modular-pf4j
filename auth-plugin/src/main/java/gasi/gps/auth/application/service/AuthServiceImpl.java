package gasi.gps.auth.application.service;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.port.inbound.AuthService;
import gasi.gps.auth.domain.port.outbound.UserRepositoryPort;
import gasi.gps.auth.infrastructure.security.JwtUtil;
import gasi.gps.core.api.application.exception.BusinessException;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepositoryPort userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepositoryPort userRepository,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public LoginResponse login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        String accessToken = jwtUtil.generateAccessToken(
                user.getId().toString(), user.getUsername(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        // Update last login
        user.setLastLoginAt(Instant.now());
        user.setFailedLoginCount(0);
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new BusinessException("User not found"));

        Set<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet())
                : Set.of();

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId().toString(), user.getUsername(), roles);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }
}
