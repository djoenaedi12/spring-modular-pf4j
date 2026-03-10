package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.domain.model.PasswordReset;
import gasi.gps.auth.infrastructure.entity.PasswordResetEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface PasswordResetMapper extends BaseMapper<PasswordReset, PasswordResetEntity> {

}
