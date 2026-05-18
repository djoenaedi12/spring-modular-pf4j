package gasi.gps.storage.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gasi.gps.core.starter.application.mapper.IgnoreAuditFields;
import gasi.gps.core.starter.infrastructure.mapper.BaseMapper;
import gasi.gps.storage.domain.model.StorageProvider;
import gasi.gps.storage.infrastructure.entity.StorageProviderEntity;

/**
 * MapStruct mapper between {@link StorageProvider} and {@link StorageProviderEntity}.
 *
 * <p>The {@code config} field is stored as a JSON string in the domain model
 * but as a {@code Map} in the entity. Conversion is handled manually.</p>
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface StorageProviderMapper extends BaseMapper<StorageProvider, StorageProviderEntity> {

    @Override
    @Mapping(target = "config", ignore = true)
    StorageProviderEntity toEntity(StorageProvider domain);

    @Override
    @Mapping(target = "config", ignore = true)
    StorageProvider toDomain(StorageProviderEntity entity);

    @Override
    @IgnoreAuditFields
    @Mapping(target = "config", ignore = true)
    void updateEntity(StorageProvider source, @MappingTarget StorageProviderEntity target);
}
