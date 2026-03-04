package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.LoginResponse;

/**
 * Inbound port for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticate a user with username and password.
     *
     * @param username the username
     * @param password the raw password
     * @return login response containing tokens
     */
    LoginResponse login(String username, String password);

    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return new login response with fresh tokens
     */
    LoginResponse refreshToken(String refreshToken);
}
