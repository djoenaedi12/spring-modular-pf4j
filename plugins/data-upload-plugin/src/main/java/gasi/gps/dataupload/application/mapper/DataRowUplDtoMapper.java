package gasi.gps.dataupload.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.starter.application.mapper.BaseReadDtoMapper;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import gasi.gps.dataupload.application.dto.DataRowUplDetailResponse;
import gasi.gps.dataupload.application.dto.DataRowUplSummaryResponse;
import gasi.gps.dataupload.domain.model.DataRowUpl;

/**
 * Mapper for upload row read responses.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface DataRowUplDtoMapper
        extends BaseReadDtoMapper<DataRowUpl, DataRowUplSummaryResponse, DataRowUplDetailResponse> {
}
