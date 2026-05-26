package gasi.gps.dataupload.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.starter.application.mapper.BaseReadDtoMapper;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import gasi.gps.dataupload.application.dto.DataUplDetailResponse;
import gasi.gps.dataupload.application.dto.DataUplSummaryResponse;
import gasi.gps.dataupload.domain.model.DataUpl;

/**
 * Mapper for upload header read responses.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface DataUplDtoMapper
        extends BaseReadDtoMapper<DataUpl, DataUplSummaryResponse, DataUplDetailResponse> {
}
