package gasi.gps.auth.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing an action (e.g. CREATE, READ, UPDATE, DELETE).
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Action extends BaseModel {

    private String code;
    private String name;
    private String description;
}
