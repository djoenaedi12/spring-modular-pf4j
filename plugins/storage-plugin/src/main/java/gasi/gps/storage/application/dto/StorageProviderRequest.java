package gasi.gps.storage.application.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a storage provider.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageProviderRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String providerType;

    @NotNull
    private Map<String, Object> config;

    private boolean isDefault;
    private boolean enabled;
}
