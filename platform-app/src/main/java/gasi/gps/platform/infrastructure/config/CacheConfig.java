package gasi.gps.platform.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables application-level caching.
 *
 * <p>
 * Cache provider settings are configured through Spring Boot's
 * {@code spring.cache.*} properties.
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
