package gasi.gps.auth.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gasi.gps.auth.domain.model.PasswordHistory;
import gasi.gps.auth.infrastructure.entity.PasswordHistoryEntity;
import gasi.gps.auth.infrastructure.entity.UserEntity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface PasswordHistoryMapper extends BaseMapper<PasswordHistory, PasswordHistoryEntity> {

    @Override
    @Mapping(target = "userId", source = "user.id")
    PasswordHistory toDomain(PasswordHistoryEntity entity);

    @Override
    @Mapping(target = "user", source = "userId")
    PasswordHistoryEntity toEntity(PasswordHistory domain);

    default UserEntity map(Long userId) {
        if (userId == null) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        return userEntity;
    }
}
