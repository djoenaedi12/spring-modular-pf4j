package gasi.gps.auth.application.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class RoleDtoMapper implements
        BaseDtoMapper<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse> {

    @Autowired
    protected IdEncoder idHasher;

    @Named("rolesToIds")
    public Set<String> rolesToIds(java.util.Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(role -> idHasher.encode(role.getId()))
                .collect(Collectors.toSet());
    }
}
