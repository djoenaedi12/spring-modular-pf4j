package gasi.gps.auth.infrastructure.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link RoleRecordRuleEntity}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRecordRuleId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long role;
    private Long recordRule;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RoleRecordRuleId that = (RoleRecordRuleId) o;
        return Objects.equals(role, that.role)
                && Objects.equals(recordRule, that.recordRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, recordRule);
    }
}
