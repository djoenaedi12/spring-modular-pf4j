package gasi.gps.audit.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.audit.application.dto.AuditLogCreateRequest;
import gasi.gps.audit.application.dto.AuditLogDetailResponse;
import gasi.gps.audit.application.dto.AuditLogSummaryResponse;
import gasi.gps.audit.application.dto.AuditLogUpdateRequest;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

/**
 * MapStruct mapper for AuditLog DTOs ↔ domain model conversions.
 */
@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface AuditLogDtoMapper extends
        BaseDtoMapper<AuditLog, AuditLogCreateRequest, AuditLogUpdateRequest, AuditLogSummaryResponse, AuditLogDetailResponse> {
}
