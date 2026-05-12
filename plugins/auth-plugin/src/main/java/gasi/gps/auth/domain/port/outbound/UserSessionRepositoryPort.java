package gasi.gps.auth.domain.port.outbound;

import gasi.gps.auth.domain.model.UserSession;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for login session persistence.
 */
public interface UserSessionRepositoryPort extends BaseRepositoryPort<UserSession> {
}
