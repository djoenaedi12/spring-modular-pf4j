package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.RoleRecordRule;
import gasi.gps.auth.infrastructure.entity.RoleRecordRuleEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(uses = { RoleMapper.class, RecordRuleMapper.class })
public interface RoleRecordRuleMapper extends BaseMapper<RoleRecordRule, RoleRecordRuleEntity> {
}
