package gasi.gps.core.api.presentation.dto;

import java.util.List;

import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.SortOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for search endpoints, combining a polymorphic filter with sort
 * orders.
 *
 * <p>
 * Example JSON:
 * </p>
 *
 * <pre>{@code
 * {
 * "filter": {
 * "type": "simple",
 * "field": "name",
 * "operator": "LIKE",
 * "value": "%admin%"
 * },
 * "sorts": [
 * { "field": "createdAt", "direction": "DESC" }
 * ]
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private GenericFilter filter;
    private List<SortOrder> sorts;
    @Default
    private Integer page = 0;
    @Default
    private Integer size = 10;
}
