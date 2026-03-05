package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.infrastructure.entity.RecordRuleEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(uses = { ResourceMapper.class })
public interface RecordRuleMapper extends BaseMapper<RecordRule, RecordRuleEntity> {
}
