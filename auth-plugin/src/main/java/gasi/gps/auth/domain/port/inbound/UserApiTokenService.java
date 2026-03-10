package gasi.gps.auth.domain.port.inbound;

import java.util.Optional;

import gasi.gps.auth.application.dto.UserApiTokenCreateRequest;
import gasi.gps.auth.application.dto.UserApiTokenDetailResponse;
import gasi.gps.auth.application.dto.UserApiTokenSummaryResponse;
import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.infrastructure.security.AuthenticatedPrincipal;

/**
 * Inbound port for user API token operations.
 */
public interface UserApiTokenService extends
        BaseService<UserApiToken, UserApiTokenCreateRequest, UserApiTokenCreateRequest, UserApiTokenSummaryResponse, UserApiTokenDetailResponse> {
    /**
     * Validates raw API token and returns authenticated principal.
     */
    Optional<AuthenticatedPrincipal> authenticate(String rawToken);
}
