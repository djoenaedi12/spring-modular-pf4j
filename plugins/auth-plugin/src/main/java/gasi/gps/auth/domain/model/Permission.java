package gasi.gps.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a permission (role-action-resource triple).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private Role role;
    private Action action;
    private Resource resource;
}
