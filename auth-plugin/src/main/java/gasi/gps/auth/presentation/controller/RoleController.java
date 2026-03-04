package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.port.inbound.RoleService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/roles")
public class RoleController extends
        BaseController<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse> {

    public RoleController(RoleService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }
}
