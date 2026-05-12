package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for role CRUD operations.
 */
public interface RoleService extends
        BaseService<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse> {
}
