package gasi.gps.storage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for storage provider mappings.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageProviderMappingResponse {

    private String id;
    private String resource;
    private String providerId;
}
