package gasi.gps.audit.presentation.controller;

import gasi.gps.audit.application.dto.AuditLogCreateRequest;
import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.application.dto.AuditLogUpdateRequest;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.inbound.AuditLogService;
import gasi.gps.core.starter.presentation.controller.BaseController;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

public class AuditLogController extends
        BaseController<AuditLog, AuditLogCreateRequest, AuditLogUpdateRequest, AuditLogSummaryResponse, AuditLogDetailResponse> {

    public AuditLogController(AuditLogService service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }

    @Override
    public String getResourceName() {
        return "AuditLog";
    }
}
