package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.auth.infrastructure.entity.UserApiTokenEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.mapper.IgnoreAuditFields;

/**
 * Mapper for user API token domain/entity.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface UserApiTokenMapper extends BaseMapper<UserApiToken, UserApiTokenEntity> {

    @Override
    @IgnoreAuditFields
    void updateEntity(UserApiToken source, @MappingTarget UserApiTokenEntity target);
}
