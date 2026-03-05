package gasi.gps.audit.application.service;

import gasi.gps.audit.application.dto.AuditLogCreateRequest;
import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.application.dto.AuditLogUpdateRequest;
import gasi.gps.audit.application.mapper.AuditLogDtoMapper;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.inbound.AuditLogService;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

public class AuditLogServiceImpl extends
        BaseServiceImpl<AuditLog, AuditLogCreateRequest, AuditLogUpdateRequest, AuditLogSummaryResponse, AuditLogDetailResponse>
        implements AuditLogService {

    public AuditLogServiceImpl(AuditLogRepositoryPort repositoryPort,
            AuditLogDtoMapper mapper,
            MessageUtil messageUtil, IdEncoder idEncoder) {
        super(repositoryPort, mapper, messageUtil, idEncoder);
    }

    @Override
    protected String resourceType() {
        return "AuditLog";
    }
}
