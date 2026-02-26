package gasi.gps.api.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain-level sort order definition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortOrder {
    private String field;
    private Direction direction;

    public enum Direction {
        ASC, DESC
    }

    public static SortOrder asc(String field) {
        return new SortOrder(field, Direction.ASC);
    }

    public static SortOrder desc(String field) {
        return new SortOrder(field, Direction.DESC);
    }
}
