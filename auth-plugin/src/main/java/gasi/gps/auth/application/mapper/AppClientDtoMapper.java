package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.auth.application.dto.AppClientCreateRequest;
import gasi.gps.auth.application.dto.AppClientDetailResponse;
import gasi.gps.auth.application.dto.AppClientSummaryResponse;
import gasi.gps.auth.application.dto.AppClientUpdateRequest;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;

@Mapper
public interface AppClientDtoMapper extends
        BaseDtoMapper<AppClient, AppClientCreateRequest, AppClientUpdateRequest, AppClientSummaryResponse, AppClientDetailResponse> {

    @Override
    @IgnoreAuditFields
    AppClient toCreateDomain(AppClientCreateRequest createRequest);

    @Override
    @IgnoreAuditFields
    AppClient toUpdateDomain(AppClientUpdateRequest updateRequest);

    @Override
    AppClientSummaryResponse toSummary(AppClient domain);

    @Override
    AppClientDetailResponse toDetail(AppClient domain);

    @Override
    @IgnoreAuditFields
    void updateDomain(AppClientUpdateRequest updateRequest, @MappingTarget AppClient domain);
}
