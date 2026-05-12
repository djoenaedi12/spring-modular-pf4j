package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.ActionCreateRequest;
import gasi.gps.auth.application.dto.ActionDetailResponse;
import gasi.gps.auth.application.dto.ActionSummaryResponse;
import gasi.gps.auth.application.dto.ActionUpdateRequest;
import gasi.gps.auth.domain.model.Action;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for action CRUD operations.
 */
public interface ActionService extends
        BaseService<Action, ActionCreateRequest, ActionUpdateRequest, ActionSummaryResponse, ActionDetailResponse> {
}
