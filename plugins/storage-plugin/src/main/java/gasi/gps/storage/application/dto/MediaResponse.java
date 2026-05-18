package gasi.gps.storage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for media file metadata.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    private String fileKey;
    private String originalName;
    private String contentType;
    private long fileSize;
    private String checksum;
    private String resource;
    private String resourceId;
}
