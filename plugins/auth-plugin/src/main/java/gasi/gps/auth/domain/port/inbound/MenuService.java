package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.MenuCreateRequest;
import gasi.gps.auth.application.dto.MenuDetailResponse;
import gasi.gps.auth.application.dto.MenuSummaryResponse;
import gasi.gps.auth.application.dto.MenuUpdateRequest;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for menu CRUD operations.
 */
public interface MenuService extends
        BaseService<Menu, MenuCreateRequest, MenuUpdateRequest, MenuSummaryResponse, MenuDetailResponse> {
}
