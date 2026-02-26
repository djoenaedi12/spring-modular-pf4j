package gasi.gps.api.shared.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Combines multiple filters with AND logic.
 * All child filters must match for a record to be included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AndFilter extends GenericFilter {
    private List<GenericFilter> filters;
}
