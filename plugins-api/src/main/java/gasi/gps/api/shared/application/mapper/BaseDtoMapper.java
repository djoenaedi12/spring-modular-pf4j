package gasi.gps.api.shared.application.mapper;

import gasi.gps.api.shared.domain.model.BaseModel;

/**
 * Generic mapper contract for application DTOs ↔ domain models.
 *
 * @param <D>   domain model type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SUM> summary response DTO type (for lists)
 * @param <DET> detail response DTO type (for single entity)
 */
public interface BaseDtoMapper<D extends BaseModel<?>, CRQ, URQ, SUM, DET> {
    D toCreateDomain(CRQ createRequest);

    D toUpdateDomain(URQ updateRequest);

    SUM toSummary(D domain);

    DET toDetail(D domain);

    void updateDomain(URQ updateRequest, @org.mapstruct.MappingTarget D domain);
}
