package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.domain.model.UserDevice;
import gasi.gps.auth.infrastructure.entity.UserDeviceEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;
import gasi.gps.core.api.infrastructure.mapper.IgnoreAuditFields;

@Mapper(uses = { UserMapper.class, AppClientMapper.class })
public interface UserDeviceMapper extends BaseMapper<UserDevice, UserDeviceEntity> {

    @IgnoreAuditFields
    void updateEntity(UserDevice source, @MappingTarget UserDeviceEntity target);
}
