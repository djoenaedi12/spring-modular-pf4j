package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.AppClientCreateRequest;
import gasi.gps.auth.application.dto.AppClientDetailResponse;
import gasi.gps.auth.application.dto.AppClientSummaryResponse;
import gasi.gps.auth.application.dto.AppClientUpdateRequest;
import gasi.gps.auth.domain.model.AppClient;
import gasi.gps.auth.domain.port.inbound.AppClientService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/app-clients")
public class AppClientController extends
        BaseController<AppClient, AppClientCreateRequest, AppClientUpdateRequest, AppClientSummaryResponse, AppClientDetailResponse> {

    public AppClientController(AppClientService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }
}
