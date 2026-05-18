package gasi.gps.core.starter.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.api.application.dto.DataUplDetailResponse;
import gasi.gps.core.api.application.dto.DataUplSummaryResponse;
import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Mapper for upload header read responses.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface DataUplDtoMapper
        extends BaseReadDtoMapper<DataUpl, DataUplSummaryResponse, DataUplDetailResponse> {
}
