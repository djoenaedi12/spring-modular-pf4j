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
 * Request body for search endpoints.
 *
 * <p>The request combines an optional polymorphic filter, optional sort orders,
 * and optional page settings. Field names are public API field names and may be
 * restricted by the persistence adapter.</p>
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
 * "value": "admin"
 * },
 * "sorts": [
 * { "field": "createdAt", "direction": "DESC" }
 * ],
 * "fields": ["id", "code", "name"]
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
    /**
     * Optional response projection for search list/page endpoints.
     *
     * <p>When provided, only these public DTO fields are returned. Controllers
     * may force required fields such as {@code id} to remain present.</p>
     */
    private List<String> fields;
    @Default
    private Integer page = 0;
    @Default
    private Integer size = 10;
}
