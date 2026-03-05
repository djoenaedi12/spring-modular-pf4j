package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.RoleRecordRule;
import gasi.gps.auth.infrastructure.entity.RoleRecordRuleEntity;

@Mapper(uses = { RoleMapper.class, RecordRuleMapper.class })
public interface RoleRecordRuleMapper {

    RoleRecordRule toDomain(RoleRecordRuleEntity entity);

    RoleRecordRuleEntity toEntity(RoleRecordRule domain);
}
