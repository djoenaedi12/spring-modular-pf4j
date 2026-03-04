package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import gasi.gps.auth.application.dto.UserCreateRequest;
import gasi.gps.auth.application.dto.UserDetailResponse;
import gasi.gps.auth.application.dto.UserSummaryResponse;
import gasi.gps.auth.application.dto.UserUpdateRequest;
import gasi.gps.auth.domain.model.User;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class, RoleDtoMapper.class })
public abstract class UserDtoMapper implements
        BaseDtoMapper<User, UserCreateRequest, UserUpdateRequest, UserSummaryResponse, UserDetailResponse> {

    @Autowired
    protected IdEncoder idHasher;

    @Override
    @IgnoreAuditFields
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginCount", constant = "0")
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "authorizedUntil", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    public abstract User toCreateDomain(UserCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginCount", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "authorizedUntil", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    public abstract User toUpdateDomain(UserUpdateRequest updateRequest);

    @Override
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    @Mapping(target = "roleIds", source = "roles", qualifiedByName = "rolesToIds")
    public abstract UserDetailResponse toDetail(User domain);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginCount", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "authorizedUntil", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    public abstract void updateDomain(UserUpdateRequest updateRequest, @MappingTarget User domain);
}
