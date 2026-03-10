package gasi.gps.auth.domain.port.outbound;

import gasi.gps.auth.domain.model.UserApiToken;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for user API token persistence.
 */
public interface UserApiTokenRepositoryPort extends BaseRepositoryPort<UserApiToken> {
}
