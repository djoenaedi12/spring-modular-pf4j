package gasi.gps.api.shared.infrastructure.mapper;

import gasi.gps.api.shared.domain.model.BaseModel;
import gasi.gps.api.shared.infrastructure.entity.BaseEntity;

/**
 * Generic mapper contract between domain model and JPA entity.
 * Implementations should use MapStruct {@code @Mapper} annotation.
 *
 * @param <D> domain model type
 * @param <E> JPA entity type
 */
public interface BaseMapper<D extends BaseModel<?>, E extends BaseEntity> {
    D toDomain(E entity);

    E toEntity(D domain);
}
