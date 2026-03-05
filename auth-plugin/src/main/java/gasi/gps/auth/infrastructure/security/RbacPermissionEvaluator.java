package gasi.gps.auth.infrastructure.security;

import java.io.Serializable;
import java.util.Set;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import gasi.gps.auth.application.service.PermissionCacheService;
import gasi.gps.core.api.presentation.controller.BaseController;

/**
 * Custom PermissionEvaluator implementation for RBAC validation.
 * Supports verifying "RESOURCE:ACTION" permission patterns dynamically
 * primarily via the BaseController.
 */
@Component
public class RbacPermissionEvaluator implements PermissionEvaluator {

    private final PermissionCacheService permissionCacheService;

    public RbacPermissionEvaluator(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }

        String resourceType;
        if (targetDomainObject instanceof BaseController<?, ?, ?, ?, ?>) {
            resourceType = ((BaseController<?, ?, ?, ?, ?>) targetDomainObject).getResourceName();
            if (resourceType == null || resourceType.isBlank()) {
                return false;
            }
        } else if (targetDomainObject instanceof String) {
            resourceType = (String) targetDomainObject;
        } else {
            // Fallback for other usage types if necessary
            resourceType = targetDomainObject.getClass().getSimpleName().toUpperCase();
        }

        String action = ((String) permission).toUpperCase();
        String requiredPermission = resourceType.toUpperCase() + ":" + action;

        return hasPrivilege(authentication, requiredPermission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        if (authentication == null || targetType == null || !(permission instanceof String)) {
            return false;
        }

        String requiredPermission = targetType.toUpperCase() + ":" + ((String) permission).toUpperCase();
        return hasPrivilege(authentication, requiredPermission);
    }

    private boolean hasPrivilege(Authentication authentication, String requiredPermission) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String roleCode = authority.getAuthority();
            Set<String> permissions = permissionCacheService.getPermissionsByRoleCode(roleCode);
            if (permissions != null && permissions.contains(requiredPermission)) {
                return true;
            }
        }
        return false;
    }
}
