package gasi.gps.auth.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing a menu item in the navigation hierarchy.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseModel {

    private String code;
    private String name;
    private String title;
    private Menu parent;
    private String path;
    private String icon;
    private Integer sortOrder;
    private MenuType type;
    private Platform platform;
    private String module;
    private Boolean isVisible;
    private String helpUrl;
}
