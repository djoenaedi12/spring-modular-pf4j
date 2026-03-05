package gasi.gps.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model linking a role to a record rule with optional negation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRecordRule {

    private Role role;
    private RecordRule recordRule;
    private Boolean isNegated;
}
