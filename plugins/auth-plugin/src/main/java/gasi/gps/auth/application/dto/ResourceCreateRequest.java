package gasi.gps.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;

    @Builder.Default
    private Boolean isApprovalRequired = false;

    @NotBlank
    private String menuId;
}
