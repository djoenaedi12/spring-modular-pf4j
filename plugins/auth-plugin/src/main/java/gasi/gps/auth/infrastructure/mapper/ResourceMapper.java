package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.infrastructure.entity.ResourceEntity;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;

@Mapper(uses = { MenuMapper.class })
public interface ResourceMapper extends BaseMapper<Resource, ResourceEntity> {
}
