package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.MenuCreateRequest;
import gasi.gps.auth.application.dto.MenuDetailResponse;
import gasi.gps.auth.application.dto.MenuSummaryResponse;
import gasi.gps.auth.application.dto.MenuUpdateRequest;
import gasi.gps.auth.domain.model.Menu;
import gasi.gps.auth.domain.port.inbound.MenuService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/menus")
public class MenuController extends
        BaseController<Menu, MenuCreateRequest, MenuUpdateRequest, MenuSummaryResponse, MenuDetailResponse> {

    public MenuController(MenuService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "Menu";
    }
}
