package gasi.gps.core.starter.infrastructure.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.mapstruct.Mapping;

/**
 * MapStruct meta-annotation that protects common entity audit fields.
 *
 * <p>Use this on entity update methods so domain updates cannot overwrite
 * persistence identity, audit metadata, lifecycle state, or optimistic locking
 * values.</p>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "version", ignore = true)
@Mapping(target = "sourceId", ignore = true)
@Mapping(target = "lifecycleStatus", ignore = true)
public @interface IgnoreAuditFields {
}
