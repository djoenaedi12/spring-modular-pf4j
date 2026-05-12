package gasi.gps.auth.application.dto;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single resource.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceDetailResponse extends BaseDetailResponse {

    private String code;
    private String name;
    private String description;
    private Boolean isApprovalRequired;
    private String menuId;
}
