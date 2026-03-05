package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.Permission;
import gasi.gps.auth.infrastructure.entity.PermissionEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(uses = { RoleMapper.class, ActionMapper.class, ResourceMapper.class })
public interface PermissionMapper extends BaseMapper<Permission, PermissionEntity> {
}
