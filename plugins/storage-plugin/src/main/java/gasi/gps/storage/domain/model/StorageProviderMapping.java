package gasi.gps.storage.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model for resource-to-provider mapping.
 *
 * <p>Maps a specific resource type (e.g. {@code "USER_AVATAR"})
 * to a target storage provider. Unmapped resources fall back to
 * the default provider.</p>
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorageProviderMapping extends BaseModel {

    private String resource;
    private Long providerId;
}
