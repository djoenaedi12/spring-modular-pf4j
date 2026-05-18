package gasi.gps.storage.application.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for storage provider metadata.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageProviderResponse {

    private String id;
    private String code;
    private String name;
    private String providerType;
    private Map<String, Object> config;
    private boolean isDefault;
    private boolean enabled;
}
