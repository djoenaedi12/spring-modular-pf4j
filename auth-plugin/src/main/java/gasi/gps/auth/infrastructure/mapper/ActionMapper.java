package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.Action;
import gasi.gps.auth.infrastructure.entity.ActionEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface ActionMapper extends BaseMapper<Action, ActionEntity> {
}
