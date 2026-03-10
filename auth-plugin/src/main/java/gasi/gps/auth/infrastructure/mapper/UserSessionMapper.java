package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.auth.infrastructure.entity.UserSessionEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.mapper.IgnoreAuditFields;

@Mapper(uses = { UserMapper.class, AppClientMapper.class, UserDeviceMapper.class })
public interface UserSessionMapper extends BaseMapper<UserSession, UserSessionEntity> {

    @IgnoreAuditFields
    void updateEntity(UserSession source, @MappingTarget UserSessionEntity target);
}
