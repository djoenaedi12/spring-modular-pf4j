package gasi.gps.audit.infrastructure.aspect;

import java.lang.reflect.Method;
import java.time.Instant;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import gasi.gps.audit.AuditContext;
import gasi.gps.audit.domain.model.AuditLog;
import gasi.gps.audit.domain.port.outbound.AuditLogRepositoryPort;
import gasi.gps.core.api.audit.Auditable;
import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import gasi.gps.core.api.infrastructure.security.SecurityContextUtil;

/**
 * AOP Aspect for method-level @Auditable annotation.
 * Used for non-CRUD operations like APPROVE, REJECT, EXPORT, LOGIN, etc.
 *
 * <p>
 * Supports SpEL expressions in the description field:
 * - #{#paramName} — reference method parameters
 * - #{#result.field} — reference return value fields
 * </p>
 */
@Aspect
@Component
@Order(2)
public class AuditMethodAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditMethodAspect.class);

    private final AuditLogRepositoryPort repository;
    private final SecurityContextUtil securityContextUtil;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditMethodAspect(AuditLogRepositoryPort repository, SecurityContextUtil securityContextUtil) {
        this.repository = repository;
        this.securityContextUtil = securityContextUtil;
    }

    private String resolveEntityId(Object target) {
        if (target instanceof BaseEntity entity) {
            return entity.getId() != null ? entity.getId().toString() : null;
        }
        return null;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void audit(JoinPoint joinPoint, Auditable auditable, Object result) {
        // Nested call control
        if (AuditContext.isActive() && !auditable.alwaysLog()) {
            return;
        }

        boolean isRoot = !AuditContext.isActive();
        try {
            if (isRoot) {
                AuditContext.start();
            }

            String description = resolveDescription(auditable.description(), joinPoint, result);
            String entityId = resolveEntityId(result);

            repository.save(AuditLog.builder()
                    .traceId(MDC.get("traceId"))
                    .actorId(securityContextUtil.getCurrentUsername())
                    .actorIp(securityContextUtil.getCurrentIp())
                    .action(auditable.action())
                    .category(auditable.category())
                    .resourceType(result != null ? result.getClass().getSimpleName() : null)
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

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void auditFailure(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        try {
            repository.save(AuditLog.builder()
                    .traceId(MDC.get("traceId"))
                    .actorId(securityContextUtil.getCurrentUsername())
                    .actorIp(securityContextUtil.getCurrentIp())
                    .action(auditable.action())
                    .category(auditable.category())
                    .description("Failed: " + ex.getMessage())
                    .status("FAILED")
                    .createdAt(Instant.now())
                    .build());
        } catch (Exception logEx) {
            log.error("Failed to write audit failure log", logEx);
        }
    }

    /**
     * Resolve SpEL expressions in the description string.
     * Expressions are wrapped in #{...}
     * Example: "Approve leave request #{#id}" resolves #id from method params.
     */
    private String resolveDescription(String template, JoinPoint joinPoint, Object result) {
        if (template == null || template.isEmpty()) {
            return null;
        }

        if (!template.contains("#{")) {
            return template;
        }

        try {
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            Method method = sig.getMethod();

            StandardEvaluationContext context = new MethodBasedEvaluationContext(
                    null, method, joinPoint.getArgs(), paramDiscoverer);
            context.setVariable("result", result);

            // Replace all #{...} occurrences
            String resolved = template;
            while (resolved.contains("#{")) {
                int start = resolved.indexOf("#{");
                int end = resolved.indexOf("}", start);
                if (end == -1) {
                    break;
                }

                String expression = resolved.substring(start + 2, end);
                Object value = spelParser.parseExpression(expression).getValue(context);
                resolved = resolved.substring(0, start)
                        + (value != null ? value.toString() : "null")
                        + resolved.substring(end + 1);
            }
            return resolved;
        } catch (Exception e) {
            log.warn("Failed to resolve SpEL in audit description: {}", template, e);
            return template;
        }
    }
}
