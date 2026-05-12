package gasi.gps.auth.domain.port.outbound;

import java.util.List;

import gasi.gps.auth.domain.model.PasswordHistory;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for password history persistence.
 */
public interface PasswordHistoryRepositoryPort extends BaseRepositoryPort<PasswordHistory> {

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
