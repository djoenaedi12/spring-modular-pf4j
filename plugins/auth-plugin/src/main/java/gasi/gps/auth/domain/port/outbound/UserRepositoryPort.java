package gasi.gps.auth.domain.port.outbound;

import java.util.Optional;

import gasi.gps.auth.domain.model.User;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for user persistence.
 */
public interface UserRepositoryPort extends BaseRepositoryPort<User> {

    /**
     * Find a user by username.
     *
     * @param username the username to search
     * @return optional user
     */
    Optional<User> findByUsername(String username);
}
