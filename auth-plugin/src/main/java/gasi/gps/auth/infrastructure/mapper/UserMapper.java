package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gasi.gps.auth.domain.model.User;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(uses = RoleMapper.class)
public interface UserMapper extends BaseMapper<User, UserEntity> {

    @Override
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleEntitiesToDomain")
    User toDomain(UserEntity entity);

    @Override
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToEntities")
    UserEntity toEntity(User domain);
}
