package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.UserCreateRequest;
import gasi.gps.auth.application.dto.UserDetailResponse;
import gasi.gps.auth.application.dto.UserSummaryResponse;
import gasi.gps.auth.application.dto.UserUpdateRequest;
import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.domain.port.inbound.UserService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/users")
public class UserController extends
        BaseController<User, UserCreateRequest, UserUpdateRequest, UserSummaryResponse, UserDetailResponse> {

    public UserController(UserService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "User";
    }
}
