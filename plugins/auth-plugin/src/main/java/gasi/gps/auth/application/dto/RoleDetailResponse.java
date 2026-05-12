package gasi.gps.auth.application.dto;

import java.util.List;
import java.util.Set;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single role.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleDetailResponse extends BaseDetailResponse {

    private String code;
    private String name;
    private String description;
    private List<PermissionResponse> permissions;
    private List<RoleRecordRuleResponse> recordRules;
    private Set<String> menuIds;
}
