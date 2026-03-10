package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.PasswordHistory;
import gasi.gps.auth.infrastructure.entity.PasswordHistoryEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface PasswordHistoryMapper extends BaseMapper<PasswordHistory, PasswordHistoryEntity> {

}
