package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.UserApiTokenCreateRequest;
import gasi.gps.auth.application.dto.UserApiTokenDetailResponse;
import gasi.gps.auth.application.dto.UserApiTokenSummaryResponse;
import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.auth.domain.port.inbound.UserApiTokenService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

/**
 * REST controller for user API token lifecycle.
 */
@RestController
@RequestMapping("/api/v1/users/api-tokens")
public class UserApiTokenController extends
        BaseController<UserApiToken, UserApiTokenCreateRequest, UserApiTokenCreateRequest, UserApiTokenSummaryResponse, UserApiTokenDetailResponse> {

    public UserApiTokenController(UserApiTokenService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "UserApiToken";
    }

}
