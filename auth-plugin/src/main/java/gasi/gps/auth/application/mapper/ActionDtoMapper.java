package gasi.gps.auth.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.auth.application.dto.ActionCreateRequest;
import gasi.gps.auth.application.dto.ActionDetailResponse;
import gasi.gps.auth.application.dto.ActionSummaryResponse;
import gasi.gps.auth.application.dto.ActionUpdateRequest;
import gasi.gps.auth.domain.model.Action;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface ActionDtoMapper extends
        BaseDtoMapper<Action, ActionCreateRequest, ActionUpdateRequest, ActionSummaryResponse, ActionDetailResponse> {
}
