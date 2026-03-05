package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.infrastructure.entity.MenuEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface MenuMapper extends BaseMapper<Menu, MenuEntity> {
}
