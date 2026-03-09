package gasi.gps.core.api.infrastructure.mapper;

import org.mapstruct.MappingTarget;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.infrastructure.entity.BaseEntity;

/**
 * Generic mapper contract between domain model and JPA entity.
 * Implementations should use MapStruct {@code @Mapper} annotation.
 *
 * @param <D> domain model type
 * @param <E> JPA entity type
 */
public interface BaseMapper<D extends BaseModel, E extends BaseEntity> {

    D toDomain(E entity);

    E toEntity(D domain);

    @IgnoreAuditFields
    void updateEntity(D source, @MappingTarget E target);
}
