package gasi.gps.core.api.storage;

import java.util.List;
import java.util.Map;

import org.pf4j.ExtensionPoint;

/**
 * PF4J extension point for storage provider plugins.
 *
 * <p>Each storage provider plugin (e.g. {@code storage-local-plugin},
 * {@code storage-minio-plugin}) implements this interface. The media
 * plugin auto-discovers all registered factories via PF4J at runtime.</p>
 *
 * <p>{@link #getProviderType()} returns a free-form string so that
 * new provider types can be introduced without modifying core-api.</p>
 *
 * @since 1.0.0
 */
public interface FileStorageProviderFactory extends ExtensionPoint {

    /**
     * Returns the provider type handled by this factory.
     *
     * <p>Must match the {@code provider_type} column stored in the
     * {@code storage_providers} table, e.g. {@code "LOCAL"}, {@code "MINIO"},
     * {@code "S3"}, {@code "GCS"}.</p>
     *
     * @return provider type identifier
     */
    String getProviderType();

    /**
     * Creates a {@link FileStorageProvider} instance from database configuration.
     *
     * @param config key-value configuration from the JSONB column
     * @return ready-to-use provider instance
     */
    FileStorageProvider create(Map<String, Object> config);

    /**
     * Validates configuration before it is persisted to the database.
     *
     * <p>Implementations should throw
     * {@link gasi.gps.core.api.application.exception.BusinessException}
     * when required keys are missing or values are invalid.</p>
     *
     * @param config configuration to validate
     */
    void validate(Map<String, Object> config);

    /**
     * Returns metadata about the configuration fields this provider requires.
     *
     * <p>Admin UIs can use this to render dynamic forms when registering
     * a new provider instance.</p>
     *
     * @return list of config field descriptors
     */
    List<ConfigField> getConfigFields();

    /**
     * Describes a single configuration field required by a provider.
     *
     * @param key          configuration key, e.g. {@code "endpoint"}
     * @param label        human-readable label, e.g. {@code "MinIO Endpoint URL"}
     * @param type         field type: {@code "STRING"}, {@code "NUMBER"},
     *                     {@code "BOOLEAN"}, or {@code "SECRET"}
     * @param required     whether the field must be supplied
     * @param defaultValue optional default value, may be {@code null}
     */
    record ConfigField(
            String key,
            String label,
            String type,
            boolean required,
            String defaultValue) { }
}
