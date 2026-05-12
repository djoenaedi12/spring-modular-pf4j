package gasi.gps.auth.domain.port.inbound;

import gasi.gps.auth.application.dto.RecordRuleCreateRequest;
import gasi.gps.auth.application.dto.RecordRuleDetailResponse;
import gasi.gps.auth.application.dto.RecordRuleSummaryResponse;
import gasi.gps.auth.application.dto.RecordRuleUpdateRequest;
import gasi.gps.auth.domain.model.RecordRule;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for record rule CRUD operations.
 */
public interface RecordRuleService extends
        BaseService<RecordRule, RecordRuleCreateRequest, RecordRuleUpdateRequest, RecordRuleSummaryResponse, RecordRuleDetailResponse> {
}
