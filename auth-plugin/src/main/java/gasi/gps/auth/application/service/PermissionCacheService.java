package gasi.gps.auth.application.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gasi.gps.auth.infrastructure.entity.PermissionEntity;
import gasi.gps.auth.infrastructure.persistence.PermissionEntityRepository;
import gasi.gps.auth.infrastructure.persistence.RoleEntityRepository;

@Service
public class PermissionCacheService {

    private final RoleEntityRepository roleRepository;
    private final PermissionEntityRepository permissionRepository;

    public PermissionCacheService(RoleEntityRepository roleRepository,
            PermissionEntityRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Loads permissions for a given role code. Returns a set of strings
     * formatted as "RESOURCE_NAME:ACTION_CODE" (e.g. "USER:READ").
     * Uses Caffeine caching configured by Spring Cache via @Cacheable.
     *
     * @param roleCode the code of the role (e.g., "SYS_ADMIN", "MANAGER")
     * @return a Set of permission strings for the role
     */
    @Cacheable(value = "rolePermissions", key = "#roleCode", unless = "#result == null")
    @Transactional(readOnly = true)
    public Set<String> getPermissionsByRoleCode(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .map(role -> {
                    List<PermissionEntity> permissions = permissionRepository.findByRoleId(role.getId());
                    if (permissions == null) {
                        return Set.<String>of();
                    }
                    return permissions.stream()
                            .map(p -> p.getResource().getName().toUpperCase() + ":"
                                    + p.getAction().getCode().toUpperCase())
                            .collect(Collectors.toSet());
                })
                .orElse(Set.of());
    }
}
