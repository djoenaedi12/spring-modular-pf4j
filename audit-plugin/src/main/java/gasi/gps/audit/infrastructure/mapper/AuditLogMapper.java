package gasi.gps.audit.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.infrastructure.entity.AuditLogEntity;
import gasi.gps.core.api.application.mapper.IgnoreAuditFields;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper extends BaseMapper<AuditLog, AuditLogEntity> {

    @IgnoreAuditFields
    void updateEntity(AuditLog source, @MappingTarget AuditLogEntity target);

}
