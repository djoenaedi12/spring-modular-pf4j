package gasi.gps.core.api.application.mapper;

import org.mapstruct.Mapping;

import gasi.gps.core.api.domain.model.BaseModel;

/**
 * Generic mapper contract for application DTOs ↔ domain models.
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 */
public interface BaseDtoMapper<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    @IgnoreAuditFields
    D toCreateDomain(CRQ createRequest);

    @IgnoreAuditFields
    D toUpdateDomain(URQ updateRequest);

    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    SRS toSummary(D domain);

    @Mapping(target = "id", source = "id", qualifiedByName = "encodeId")
    DRS toDetail(D domain);

    @IgnoreAuditFields
    void updateDomain(URQ updateRequest, @org.mapstruct.MappingTarget D domain);
}
