package gasi.gps.audit.domain.port.inbound;

import gasi.gps.audit.application.dto.AuditLogCreateRequest;
import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.application.dto.AuditLogUpdateRequest;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.core.api.domain.port.inbound.BaseService;

public interface AuditLogService extends
        BaseService<AuditLog, AuditLogCreateRequest, AuditLogUpdateRequest, AuditLogSummaryResponse, AuditLogDetailResponse> {

}
