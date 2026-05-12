package gasi.gps.audit.domain.port.outbound;

import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for persisting audit log entries.
 */
public interface AuditLogRepositoryPort extends BaseRepositoryPort<AuditLog> {

}
