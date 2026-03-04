package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.UserCreateRequest;
import gasi.gps.auth.application.dto.UserDetailResponse;
import gasi.gps.auth.application.dto.UserSummaryResponse;
import gasi.gps.auth.application.dto.UserUpdateRequest;
import gasi.gps.auth.domain.model.User;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for user CRUD operations.
 */
public interface UserService extends
        BaseService<User, UserCreateRequest, UserUpdateRequest, UserSummaryResponse, UserDetailResponse> {
}
