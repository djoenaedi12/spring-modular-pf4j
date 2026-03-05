package gasi.gps.audit.infrastructure.aspect;

import java.time.Instant;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.audit.AuditLogExtension;
import gasi.gps.core.api.audit.AuditableEntity;
import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import gasi.gps.core.api.infrastructure.security.SecurityContextUtil;

/**
 * AOP Aspect that intercepts BaseService CUD methods and generates audit logs
 * for services annotated with @AuditableEntity.
 *
 * <p>
 * Handles nested call detection via AuditContext ThreadLocal:
 * - Default (alwaysLog = false): skip logging if already inside another audited
 * call
 * - alwaysLog = true: always log regardless of nesting
 * </p>
 */
@Aspect
@Component
@Order(1)
public class AuditEntityAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditEntityAspect.class);

    private final AuditLogRepositoryPort repository;
    private final SecurityContextUtil securityContextUtil;
    private final PluginManager pluginManager;

    public AuditEntityAspect(AuditLogRepositoryPort repository,
            SecurityContextUtil securityContextUtil,
            PluginManager pluginManager) {
        this.repository = repository;
        this.securityContextUtil = securityContextUtil;
        this.pluginManager = pluginManager;
    }

    // ─── CREATE ──────────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* gasi.gps..BaseService+.create(..)) && target(target)", returning = "result")
    public void auditCreate(JoinPoint joinPoint, Object target, Object result) {
        doLog(target, "CREATE", result);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* gasi.gps..BaseService+.update(..)) && target(target)", returning = "result")
    public void auditUpdate(JoinPoint joinPoint, Object target, Object result) {
        doLog(target, "UPDATE", result);
    }

    // ─── DELETE ──────────────────────────────────────────────────────

    @AfterReturning(pointcut = "execution(* gasi.gps..BaseService+.delete(..)) && target(target)")
    public void auditDelete(JoinPoint joinPoint, Object target) {
        Object idArg = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        doLogWithId(target, "DELETE", idArg != null ? idArg.toString() : null);
    }

    // ─── FAILURE ─────────────────────────────────────────────────────

    @AfterThrowing(pointcut = "execution(* gasi.gps..BaseService+.create(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.update(..)) && target(target)"
            + " || execution(* gasi.gps..BaseService+.delete(..)) && target(target)", throwing = "ex")
    public void auditFailure(JoinPoint joinPoint, Object target, Exception ex) {
        AuditableEntity annotation = target.getClass().getAnnotation(AuditableEntity.class);
        if (annotation == null) {
            return;
        }

        String action = resolveActionFromMethod(joinPoint.getSignature().getName());

        try {
            repository.save(AuditLog.builder()
                    .traceId(MDC.get("traceId"))
                    .actorId(securityContextUtil.getCurrentUsername())
                    .actorIp(securityContextUtil.getCurrentIp())
                    .action(action)
                    .category(annotation.category())
                    .resourceType(annotation.resourceType())
                    .description("Failed: " + ex.getMessage())
                    .status("FAILED")
                    .createdAt(Instant.now())
                    .build());
        } catch (Exception logEx) {
            log.error("Failed to write audit log for failure", logEx);
        }
    }

    // ─── Core Logic ──────────────────────────────────────────────────

    private String resolveEntityId(Object target) {
        if (target instanceof BaseEntity entity) {
            return entity.getId() != null ? entity.getId().toString() : null;
        }
        return null;
    }

    private void doLog(Object target, String action, Object result) {
        doLogWithId(target, action, resolveEntityId(result));
    }

    private void doLogWithId(Object target, String action, String entityId) {
        AuditableEntity annotation = target.getClass().getAnnotation(AuditableEntity.class);
        if (annotation == null) {
            return;
        }

        // Check if this action should be audited
        if (!List.of(annotation.auditActions()).contains(action)) {
            return;
        }

        // Nested call control
        if (AuditContext.isActive() && !annotation.alwaysLog()) {
            return;
        }

        boolean isRoot = !AuditContext.isActive();
        try {
            if (isRoot) {
                AuditContext.start();
            }

            // Resolve description from plugin enrichers
            String description = resolveDescription(annotation, action, entityId);

            repository.save(AuditLog.builder()
                    .traceId(MDC.get("traceId"))
                    .actorId(securityContextUtil.getCurrentUsername())
                    .actorIp(securityContextUtil.getCurrentIp())
                    .action(action)
                    .category(annotation.category())
                    .resourceType(annotation.resourceType())
                    .resourceId(entityId)
                    .description(description)
                    .status("SUCCESS")
                    .createdAt(Instant.now())
                    .build());

        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        } finally {
            if (isRoot) {
                AuditContext.clear();
            }
        }
    }

    private String resolveDescription(AuditableEntity annotation, String action, String entityId) {
        // Try plugin enrichers first
        List<AuditLogExtension> enrichers = pluginManager.getExtensions(AuditLogExtension.class);
        for (AuditLogExtension enricher : enrichers) {
            String desc = enricher.resolveDescription(action, annotation.resourceType(), entityId);
            if (desc != null) {
                return desc;
            }
        }

        // Default description
        return action + " " + annotation.resourceType()
                + (entityId != null ? "#" + entityId : "");
    }

    private String resolveActionFromMethod(String methodName) {
        return switch (methodName) {
            case "create" -> "CREATE";
            case "update" -> "UPDATE";
            case "delete" -> "DELETE";
            default -> methodName.toUpperCase();
        };
    }
}
