package gasi.gps.storage.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.core.starter.application.mapper.IgnoreAuditFields;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.storage.domain.model.StorageProviderMapping;
import gasi.gps.storage.infrastructure.entity.StorageProviderMappingEntity;

/**
 * MapStruct mapper between {@link StorageProviderMapping} and
 * {@link StorageProviderMappingEntity}.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface StorageProviderMappingMapper
        extends BaseMapper<StorageProviderMapping, StorageProviderMappingEntity> {

    @Override
    @IgnoreAuditFields
    void updateEntity(StorageProviderMapping source,
            @MappingTarget StorageProviderMappingEntity target);
}
