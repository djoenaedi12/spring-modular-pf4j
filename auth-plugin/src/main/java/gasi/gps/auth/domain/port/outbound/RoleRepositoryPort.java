package gasi.gps.auth.domain.port.outbound;

import java.util.Optional;

import gasi.gps.auth.domain.model.Role;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for role persistence.
 */
public interface RoleRepositoryPort extends BaseRepositoryPort<Role> {

    /**
     * Find a role by its code.
     *
     * @param code the role code (e.g. ROLE_ADMIN)
     * @return optional role
     */
    Optional<Role> findByCode(String code);
}
