package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gasi.gps.auth.domain.model.PasswordReset;
import gasi.gps.auth.infrastructure.entity.PasswordResetEntity;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface PasswordResetMapper extends BaseMapper<PasswordReset, PasswordResetEntity> {

    @Override
    @Mapping(target = "userId", source = "user.id")
    PasswordReset toDomain(PasswordResetEntity entity);

    @Override
    @Mapping(target = "user", source = "userId")
    PasswordResetEntity toEntity(PasswordReset domain);

    default UserEntity map(Long userId) {
        if (userId == null) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        return userEntity;
    }
}
