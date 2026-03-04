package gasi.gps.audit.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.audit.infrastructure.entity.AuditLogEntity;
import gasi.gps.audit.infrastructure.persistence.AuditLogEntityRepository;
import gasi.gps.core.api.audit.AuditLogSpi;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

/**
 * Adapter that implements {@link AuditLogSpi} from core-api,
 * allowing other plugins to manually write audit logs.
 */
@Component
public class AuditLogRepositoryAdapter extends BaseRepositoryAdapter<AuditLog, AuditLogEntity>
        implements AuditLogRepositoryPort {

    protected AuditLogRepositoryAdapter(AuditLogEntityRepository repository,
            BaseMapper<AuditLog, AuditLogEntity> mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "AuditLog";
    }

}
