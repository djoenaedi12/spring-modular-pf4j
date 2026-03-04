package gasi.gps.auth.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import gasi.gps.auth.domain.model.Role;
import gasi.gps.auth.infrastructure.entity.RoleEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role, RoleEntity> {

    @Named("roleEntitiesToDomain")
    default Set<Role> toDomainSet(Set<RoleEntity> entities) {
        if (entities == null)
            return null;
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    @Named("rolesToEntities")
    default Set<RoleEntity> toEntitySet(Set<Role> roles) {
        if (roles == null)
            return null;
        return roles.stream()
                .map(this::toEntity)
                .collect(Collectors.toSet());
    }
}
