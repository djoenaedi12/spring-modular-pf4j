package gasi.gps.core.starter.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.core.api.application.dto.DataRowUplDetailResponse;
import gasi.gps.core.api.application.dto.DataRowUplSummaryResponse;
import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Mapper for upload row read responses.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface DataRowUplDtoMapper
        extends BaseReadDtoMapper<DataRowUpl, DataRowUplSummaryResponse, DataRowUplDetailResponse> {
}
