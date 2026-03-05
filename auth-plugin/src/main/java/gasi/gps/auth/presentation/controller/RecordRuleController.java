package gasi.gps.auth.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.auth.application.dto.RecordRuleCreateRequest;
import gasi.gps.auth.application.dto.RecordRuleDetailResponse;
import gasi.gps.auth.application.dto.RecordRuleSummaryResponse;
import gasi.gps.auth.application.dto.RecordRuleUpdateRequest;
import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.auth.domain.port.inbound.RecordRuleService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/v1/record-rules")
public class RecordRuleController extends
        BaseController<RecordRule, RecordRuleCreateRequest, RecordRuleUpdateRequest, RecordRuleSummaryResponse, RecordRuleDetailResponse> {

    public RecordRuleController(RecordRuleService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "RecordRule";
    }
}
