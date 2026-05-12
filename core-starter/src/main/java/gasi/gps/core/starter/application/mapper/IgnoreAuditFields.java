package gasi.gps.core.starter.application.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.mapstruct.Mapping;

/**
 * MapStruct meta-annotation that protects common domain audit fields.
 *
 * <p>Use this on create/update mapper methods so request payloads cannot
 * overwrite IDs, audit metadata, lifecycle state, or optimistic locking
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
