package gasi.gps.core.infrastructure.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import gasi.gps.core.api.infrastructure.security.SecurityContextUtil;

/**
 * Enables JPA auditing and provides the current auditor (username) for auditing
 * purposes.
 * <p>
 * The auditor is obtained from the {@link SecurityContextUtil}, which should be
 * implemented
 * by the authentication mechanism in use (e.g. JWT, OAuth2, etc.). If no
 * authenticated user
 * is found, it defaults to "system". This allows JPA auditing annotations like
 * {@code @CreatedBy}
 * and {@code @LastModifiedBy} to automatically populate with the current user's
 * identity.
 * <p>
 * Note: This configuration assumes that the security context is properly set up
 * and that the {@link SecurityContextUtil} bean is available in the Spring
 * context. If not, the auditor will always default to "system". Make sure to
 * implement and register a suitable {@link SecurityContextUtil} for your
 * authentication mechanism to get accurate auditing information.
 * for {@code @CreatedBy} and {@code @LastModifiedBy} fields.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    private final SecurityContextUtil securityContextUtil;

    public JpaAuditingConfig(SecurityContextUtil securityContextUtil) {
        this.securityContextUtil = securityContextUtil;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(securityContextUtil.getCurrentUsername())
                .or(() -> Optional.of("system"));
    }
}
