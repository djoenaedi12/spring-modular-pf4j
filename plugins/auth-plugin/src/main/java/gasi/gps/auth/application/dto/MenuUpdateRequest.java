package gasi.gps.auth.application.dto;

import gasi.gps.auth.domain.model.MenuType;
import gasi.gps.core.api.domain.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a menu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 150)
    private String title;

    private String parentId;

    @Size(max = 255)
    private String path;

    @Size(max = 50)
    private String icon;

    @Builder.Default
    private Integer sortOrder = 0;

    @NotNull
    private MenuType type;

    @NotNull
    private Platform platform;

    @Size(max = 50)
    private String module;

    @Builder.Default
    private Boolean isVisible = true;

    @Size(max = 255)
    private String helpUrl;
}
