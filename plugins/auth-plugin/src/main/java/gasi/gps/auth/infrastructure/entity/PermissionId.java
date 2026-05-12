package gasi.gps.auth.infrastructure.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link PermissionEntity}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long role;
    private Long action;
    private Long resource;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PermissionId that = (PermissionId) o;
        return Objects.equals(role, that.role)
                && Objects.equals(action, that.action)
                && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, action, resource);
    }
}
