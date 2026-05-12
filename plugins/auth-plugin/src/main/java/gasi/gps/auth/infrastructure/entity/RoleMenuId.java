package gasi.gps.auth.infrastructure.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link RoleMenuEntity}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long role;
    private Long menu;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RoleMenuId that = (RoleMenuId) o;
        return Objects.equals(role, that.role)
                && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, menu);
    }
}
