package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.ResourceCreateRequest;
import gasi.gps.auth.application.dto.ResourceDetailResponse;
import gasi.gps.auth.application.dto.ResourceSummaryResponse;
import gasi.gps.auth.application.dto.ResourceUpdateRequest;
import gasi.gps.auth.domain.model.Resource;
import gasi.gps.auth.domain.port.inbound.ResourceService;
import gasi.gps.core.starter.presentation.controller.BaseController;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController extends
        BaseController<Resource, ResourceCreateRequest, ResourceUpdateRequest, ResourceSummaryResponse, ResourceDetailResponse> {

    public ResourceController(ResourceService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "Resource";
    }
}
