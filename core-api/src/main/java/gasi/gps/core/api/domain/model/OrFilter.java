package gasi.gps.core.api.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Filter expression that combines child filters with logical OR.
 *
 * <p>At least one child filter must match for a record to be included in the
 * result.</p>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrFilter extends GenericFilter {
    private List<GenericFilter> filters;
}
