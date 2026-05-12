package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.AppClientCreateRequest;
import gasi.gps.auth.application.dto.AppClientDetailResponse;
import gasi.gps.auth.application.dto.AppClientSummaryResponse;
import gasi.gps.auth.application.dto.AppClientUpdateRequest;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for app client CRUD operations.
 */
public interface AppClientService extends
        BaseService<AppClient, AppClientCreateRequest, AppClientUpdateRequest, AppClientSummaryResponse, AppClientDetailResponse> {
}
