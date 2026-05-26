package gasi.gps.core.starter.presentation.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.api.presentation.dto.SearchRequest;
import gasi.gps.core.starter.presentation.support.ResponseProjection;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;

/**
 * Abstract REST controller for standard read and search endpoints.
 *
 * <p>
 * Subclasses only need to supply the concrete {@link BaseReadService} via
 * the constructor and annotate with {@code @RestController} and
 * {@code @RequestMapping}.
 * </p>
 *
 * <h2>Exposed endpoints relative to the subclass mapping</h2>
 * <table>
 * <caption>Default read and search endpoints</caption>
 * <tr>
 * <th>Method</th>
 * <th>Path</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>GET</td>
 * <td>/{id}</td>
 * <td>Get resource by ID</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/search</td>
 * <td>Find single resource by filter</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/search/list</td>
 * <td>Find all matching (list)</td>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/search/page</td>
 * <td>Find all matching (paged)</td>
 * </tr>
 * </table>
 *
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public abstract class BaseReadController<SRS, DRS> {

    /**
     * Returns the name of the resource for RBAC permission evaluation.
     * For example, returning "ROLE" will evaluate permissions like "ROLE:CREATE".
     *
     * @return the resource name.
     */
    public abstract String getResourceName();

    private final BaseReadService<SRS, DRS> service;
    private final IdEncoder idEncoder;

    /**
     * Constructs a new {@code BaseReadController}.
     *
     * @param service   the service handling business logic
     * @param idEncoder the ID encoder for encoding/decoding IDs
     */
    protected BaseReadController(BaseReadService<SRS, DRS> service,
            IdEncoder idEncoder) {
        this.service = service;
        this.idEncoder = idEncoder;
    }

    /**
     * Returns the underlying service instance.
     *
     * @return the base read service
     */
    protected BaseReadService<SRS, DRS> getService() {
        return service;
    }

    /**
     * Returns the ID encoder instance.
     *
     * @return the ID encoder
     */
    protected IdEncoder getIdEncoder() {
        return idEncoder;
    }

    /**
     * Default response projection for search list/page when callers omit fields.
     *
     * <p>
     * Subclasses should override this with resource-specific default table
     * columns. The fallback keeps only {@code id} to avoid accidentally exposing
     * a full summary DTO on unprojected requests.
     * </p>
     *
     * @return default public DTO field names
     */
    protected List<String> getDefaultSummaryFields() {
        return List.of("id");
    }

    private List<String> resolveProjectionFields(SearchRequest request) {
        return request.getFields() == null || request.getFields().isEmpty()
                ? getDefaultSummaryFields()
                : request.getFields();
    }

    /**
     * Retrieves a resource by its identifier.
     *
     * @param id the resource identifier
     * @return the resource detail wrapped in {@link ApiResponse}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DRS> findById(@PathVariable String id) {
        return ApiResponse.ok(service.findById(idEncoder.decode(id)));
    }

    /**
     * Finds a single resource matching the given filter.
     *
     * @param request the search request containing filter criteria
     * @return the matching resource detail wrapped in {@link ApiResponse}
     */
    @PostMapping("/search")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DRS> findBy(@RequestBody SearchRequest request) {
        return ApiResponse.ok(service.findBy(request.getFilter()));
    }

    /**
     * Finds all resources matching the given filter and sort orders.
     *
     * @param request the search request containing filter and sort criteria
     * @return a list of matching resource summaries wrapped in {@link ApiResponse}
     */
    @PostMapping("/search/list")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<List<?>> findAll(@RequestBody SearchRequest request) {
        List<SRS> result = service.findAll(
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(ResponseProjection.projectList(result, resolveProjectionFields(request)));
    }

    /**
     * Finds all resources matching the given filter with pagination.
     *
     * @param request the search request containing filter and sort criteria
     * @return a page of matching resource summaries
     */
    @PostMapping("/search/page")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<PageResult<?>> findAllPaged(
            @RequestBody SearchRequest request) {
        PageResult<SRS> result = service.findAll(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(ResponseProjection.projectPage(result, resolveProjectionFields(request)));
    }

    @PostMapping("/lookup/search/page")
    @PreAuthorize("hasPermission(this, 'LOOKUP')")
    public ApiResponse<PageResult<?>> lookupPaged(@RequestBody SearchRequest request) {
        PageResult<SRS> result = service.findAll(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());

        return ApiResponse.ok(ResponseProjection.projectPage(result, resolveLookupFields(request)));
    }

    protected List<String> getDefaultLookupFields() {
        return getDefaultSummaryFields();
    }

    private List<String> resolveLookupFields(SearchRequest request) {
        return request.getFields() == null || request.getFields().isEmpty()
                ? getDefaultLookupFields()
                : request.getFields();
    }
}
