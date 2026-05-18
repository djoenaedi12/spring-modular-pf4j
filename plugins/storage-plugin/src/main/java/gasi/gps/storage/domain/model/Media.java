package gasi.gps.storage.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model for uploaded media files.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Media extends BaseModel {

    private String fileKey;
    private String originalName;
    private String contentType;
    private long fileSize;
    private String storagePath;
    private Long providerId;
    private String checksum;
    private String resource;
    private Long resourceId;
}
