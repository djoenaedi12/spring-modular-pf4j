package gasi.gps.core.starter.application.mapper;

import org.mapstruct.Mapping;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Generic MapStruct contract for read-side domain to response mappings.
 *
 * <p>Concrete plugin mappers may extend this interface when they only need
 * summary and detail response conversions. The ID mappings expect an
 * {@code encodeId} MapStruct qualifier supplied by the starter ID codec.</p>
 *
 * @param <D>   domain model type
 * @param <SRS> summary response DTO type
 * @param <DRS> detail response DTO type
 * @since 1.0.0
 */
public interface BaseReadDtoMapper<D extends BaseModel, SRS, DRS> {

    /**
     * Converts a domain model into a list-friendly summary response.
     *
     * @param domain domain model
     * @return summary response with encoded public ID
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    SRS toSummary(D domain);

    /**
     * Converts a domain model into a detail response.
     *
     * @param domain domain model
     * @return detail response with encoded public ID
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    DRS toDetail(D domain);
}
