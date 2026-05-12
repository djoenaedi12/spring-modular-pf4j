package gasi.gps.auth.application.dto;

import gasi.gps.auth.domain.model.MenuType;
import gasi.gps.core.api.application.dto.BaseDetailResponse;
import gasi.gps.core.api.domain.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single menu.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MenuDetailResponse extends BaseDetailResponse {

    private String code;
    private String name;
    private String title;
    private String parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private MenuType type;
    private Platform platform;
    private String module;
    private Boolean isVisible;
    private String helpUrl;
}
