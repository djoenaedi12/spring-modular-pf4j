package gasi.gps.auth.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing a resource (an entity/feature that can be secured).
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Resource extends BaseModel {

    private String code;
    private String name;
    private String description;
    private Boolean isApprovalRequired;
    private Menu menu;
}
