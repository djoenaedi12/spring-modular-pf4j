package gasi.gps.auth.application.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.PermissionResponse;
import gasi.gps.auth.application.dto.RoleCreateRequest;
import gasi.gps.auth.application.dto.RoleDetailResponse;
import gasi.gps.auth.application.dto.RoleRecordRuleResponse;
import gasi.gps.auth.application.dto.RoleSummaryResponse;
import gasi.gps.auth.application.dto.RoleUpdateRequest;
import gasi.gps.auth.domain.model.Permission;
import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.domain.model.RoleRecordRule;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public abstract class RoleDtoMapper implements
        BaseDtoMapper<Role, RoleCreateRequest, RoleUpdateRequest, RoleSummaryResponse, RoleDetailResponse> {

    @Autowired
    protected IdEncoder idEncoder;

    @Override
    @IgnoreAuditFields
    public abstract Role toCreateDomain(RoleCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    public abstract Role toUpdateDomain(RoleUpdateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    public abstract RoleSummaryResponse toSummary(Role domain);

    @Override
    @IgnoreAuditFields
    public abstract void updateDomain(RoleUpdateRequest updateRequest, @MappingTarget Role domain);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "permissions", source = "domain", qualifiedByName = "permissionsIgnored")
    @Mapping(target = "recordRules", source = "domain", qualifiedByName = "recordRulesIgnored")
    @Mapping(target = "menuIds", ignore = true)
    public abstract RoleDetailResponse toDetail(Role domain);

    @Named("permissionsIgnored")
    protected List<PermissionResponse> permissionsIgnored(Role domain) {
        return Collections.emptyList();
    }

    @Named("recordRulesIgnored")
    protected List<RoleRecordRuleResponse> recordRulesIgnored(Role domain) {
        return Collections.emptyList();
    }

    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponse.builder()
                .actionId(permission.getAction() != null ? idEncoder.encode(permission.getAction().getId()) : null)
                .resourceId(
                        permission.getResource() != null ? idEncoder.encode(permission.getResource().getId()) : null)
                .build();
    }

    public RoleRecordRuleResponse toRoleRecordRuleResponse(RoleRecordRule roleRecordRule) {
        if (roleRecordRule == null) {
            return null;
        }
        return RoleRecordRuleResponse.builder()
                .recordRuleId(roleRecordRule.getRecordRule() != null
                        ? idEncoder.encode(roleRecordRule.getRecordRule().getId())
                        : null)
                .isNegated(roleRecordRule.getIsNegated())
                .build();
    }

    @Named("rolesToIds")
    public Set<String> rolesToIds(java.util.Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(role -> idEncoder.encode(role.getId()))
                .collect(Collectors.toSet());
    }
}
