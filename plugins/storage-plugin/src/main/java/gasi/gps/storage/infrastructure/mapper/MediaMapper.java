package gasi.gps.storage.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.core.starter.application.mapper.IgnoreAuditFields;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.storage.domain.model.Media;
import gasi.gps.storage.infrastructure.entity.MediaEntity;

/**
 * MapStruct mapper between {@link Media} domain model and {@link MediaEntity}.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface MediaMapper extends BaseMapper<Media, MediaEntity> {

    @Override
    @IgnoreAuditFields
    void updateEntity(Media source, @MappingTarget MediaEntity target);
}
