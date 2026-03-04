package gasi.gps.auth.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.application.mapper.RoleDtoMapper;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.port.inbound.RoleService;
import gasi.gps.auth.domain.port.outbound.RoleRepositoryPort;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;

@Service
public class RoleServiceImpl extends
        BaseServiceImpl<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse>
        implements RoleService {

    public RoleServiceImpl(RoleRepositoryPort repository,
            RoleDtoMapper mapper,
            MessageUtil messageUtil) {
        super(repository, mapper, messageUtil);
    }

    @Override
    protected String resourceType() {
        return "Role";
    }
}
