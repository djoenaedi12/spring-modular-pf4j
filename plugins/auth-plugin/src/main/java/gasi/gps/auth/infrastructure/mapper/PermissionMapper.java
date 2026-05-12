package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.Permission;
import gasi.gps.auth.infrastructure.entity.PermissionEntity;

@Mapper(uses = { RoleMapper.class, ActionMapper.class, ResourceMapper.class })
public interface PermissionMapper {

    Permission toDomain(PermissionEntity entity);

    PermissionEntity toEntity(Permission domain);
}
