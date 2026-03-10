package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.domain.model.UserSession;

/**
 * Inbound port for validating and revoking user sessions.
 */
public interface UserSessionService {

    /**
     * Validates that the given access token JTI exists in an active session.
     *
     * @param jti the JWT ID of the access token
     * @return true if the token is valid and the session has not expired
     */
    boolean validateAccessToken(String jti);

    /**
     * Revokes a session by its access token JTI (logout).
     *
     * @param jti the JWT ID of the access token to revoke
     */
    void revokeAccessToken(String jti);

    boolean save(UserSession userSession, String jti);
}
