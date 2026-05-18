package gasi.gps.storage.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model for a registered storage provider.
 *
 * @since 1.0.0
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StorageProvider extends BaseModel {

    private String code;
    private String name;
    private String providerType;
    private String config;
    private boolean isDefault;
    private boolean enabled;
}
