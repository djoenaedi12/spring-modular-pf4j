package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.ForgotPasswordRequest;
import gasi.gps.auth.application.dto.LoginRequest;
import gasi.gps.auth.application.dto.LoginResponse;
import gasi.gps.auth.application.dto.ResetPasswordRequest;

/**
 * Inbound port for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticate a user with LoginRequest details.
     *
     * @param request the login request containing credentials and device metadata
     * @return login response containing tokens
     */
    LoginResponse login(LoginRequest request);

    /**
     * Authenticate a user with client credentials (Basic Auth).
     *
     * <p>
     * Validates the client's {@code clientSecret}, checks that the requested
     * {@code grantType} is allowed, and generates tokens with the client's
     * configured expiry and scopes.
     *
     * @param clientId     the client identifier from Basic Auth
     * @param clientSecret the client secret from Basic Auth
     * @param request      login request containing grantType, username, password
     * @return login response containing tokens and scopes
     */
    LoginResponse login(String clientId, String clientSecret, LoginRequest request);

    /**
     * Initiates forgot password flow.
     *
     * @param request forgot password request
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets password using a valid reset token.
     *
     * @param request reset password request
     */
    void resetPassword(ResetPasswordRequest request);
}
