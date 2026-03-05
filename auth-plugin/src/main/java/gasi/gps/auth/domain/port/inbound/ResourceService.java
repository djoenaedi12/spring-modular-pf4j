package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.ResourceCreateRequest;
import gasi.gps.auth.application.dto.ResourceDetailResponse;
import gasi.gps.auth.application.dto.ResourceSummaryResponse;
import gasi.gps.auth.application.dto.ResourceUpdateRequest;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for resource CRUD operations.
 */
public interface ResourceService extends
        BaseService<Resource, ResourceCreateRequest, ResourceUpdateRequest, ResourceSummaryResponse, ResourceDetailResponse> {
}
