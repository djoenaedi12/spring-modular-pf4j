package gasi.gps.core.starter.application.mapper;

import org.mapstruct.Mapping;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Generic MapStruct contract between application DTOs and domain models.
 *
 * <p>Concrete plugin mappers extend this interface to keep create, update,
 * summary, and detail conversions consistent across modules. The ID mappings
 * expect an {@code encodeId} MapStruct qualifier supplied by the starter ID
 * codec.</p>
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public interface BaseDtoMapper<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    /**
     * Converts a create request into a new domain model.
     *
     * @param createRequest create request DTO
     * @return domain model populated from the create request
     */
    @IgnoreAuditFields
    D toCreateDomain(CRQ createRequest);

    /**
     * Converts an update request into a domain model.
     *
     * @param updateRequest update request DTO
     * @return domain model populated from the update request
     */
    @IgnoreAuditFields
    D toUpdateDomain(URQ updateRequest);

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

    /**
     * Applies an update request onto an existing domain model.
     *
     * @param updateRequest update request DTO
     * @param domain        existing domain model to mutate
     */
    @IgnoreAuditFields
    void updateDomain(URQ updateRequest, @org.mapstruct.MappingTarget D domain);
}
