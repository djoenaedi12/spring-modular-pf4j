package gasi.gps.auth.infrastructure.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * CORS configuration properties bound from {@code app.security.cors.*}.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security.cors")
public class CorsProperties {

    /** Allowed origins, e.g. http://localhost:3000 */
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    /** Allowed HTTP methods. */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE");

    /** Allowed request headers. */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-API-Key", "X-Device-Id",
            "X-Device-Model");

    /** Headers exposed to the browser. */
    private List<String> exposedHeaders = List.of("Authorization");

    /** Whether to allow cookies/credentials. */
    private boolean allowCredentials = true;

    /** Preflight cache duration in seconds. */
    private long maxAge = 86400L;
}
