package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.ActionCreateRequest;
import gasi.gps.auth.application.dto.ActionDetailResponse;
import gasi.gps.auth.application.dto.ActionSummaryResponse;
import gasi.gps.auth.application.dto.ActionUpdateRequest;
import gasi.gps.auth.domain.model.Action;
import gasi.gps.auth.domain.port.inbound.ActionService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/actions")
public class ActionController extends
        BaseController<Action, ActionCreateRequest, ActionUpdateRequest, ActionSummaryResponse, ActionDetailResponse> {

    public ActionController(ActionService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "Action";
    }
}
