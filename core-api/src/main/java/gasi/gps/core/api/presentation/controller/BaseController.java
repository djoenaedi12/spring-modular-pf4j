package gasi.gps.core.api.presentation.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.api.presentation.dto.SearchRequest;
import jakarta.validation.Valid;

/**
 * Abstract REST controller providing standard CRUD and search endpoints.
 *
 * <p>
 * Subclasses only need to supply the concrete {@link BaseService} via
 * the constructor and annotate with {@code @RestController} and
 * {@code @RequestMapping}.
 * </p>
 *
 * <h3>Exposed endpoints (relative to the subclass mapping):</h3>
 * <table>
 * <tr>
 * <th>Method</th>
 * <th>Path</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>POST</td>
 * <td>/</td>
 * <td>Create a new resource</td>
 * </tr>
 * <tr>
 * <td>GET</td>
 * <td>/{id}</td>
 * <td>Get resource by ID</td>
 * </tr>
 * <tr>
 * <td>PUT</td>
 * <td>/{id}</td>
 * <td>Update resource by ID</td>
 * </tr>
 * <tr>
 * <td>DELETE</td>
 * <td>/{id}</td>
 * <td>Delete resource by ID</td>
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
 * @param <D>   domain model type
 * @param <ID>  identifier type
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public abstract class BaseController<D extends BaseModel, CRQ, URQ, SRS, DRS> {

    /**
     * Returns the name of the resource for RBAC permission evaluation.
     * For example, returning "ROLE" will evaluate permissions like "ROLE:CREATE".
     *
     * @return the resource name.
     */
    public abstract String getResourceName();

    private final BaseService<D, CRQ, URQ, SRS, DRS> service;
    private final IdEncoder idEncoder;

    /**
     * Constructs a new {@code BaseController}.
     *
     * @param service   the service handling business logic
     * @param idEncoder the ID encoder for encoding/decoding IDs
     */
    protected BaseController(BaseService<D, CRQ, URQ, SRS, DRS> service,
            IdEncoder idEncoder) {
        this.service = service;
        this.idEncoder = idEncoder;
    }

    /**
     * Returns the underlying service instance.
     *
     * @return the base service
     */
    protected BaseService<D, CRQ, URQ, SRS, DRS> getService() {
        return service;
    }

    /**
     * Returns the ID hasher instance.
     *
     * @return the ID encoder
     */
    protected IdEncoder getIdEncoder() {
        return idEncoder;
    }

    /**
     * Creates a new resource.
     *
     * @param request the create request body
     * @return the created resource detail wrapped in {@link ApiResponse}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(this, 'CREATE')")
    public ApiResponse<DRS> create(@Valid @RequestBody CRQ request) {
        return ApiResponse.ok(service.create(request));
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
    public ApiResponse<List<SRS>> findAll(@RequestBody SearchRequest request) {
        List<SRS> result = service.findAll(
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(result);
    }

    /**
     * Finds all resources matching the given filter with pagination.
     *
     * @param request the search request containing filter and sort criteria
     * @return a page of matching resource summaries
     */
    @PostMapping("/search/page")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<PageResult<SRS>> findAllPaged(
            @RequestBody SearchRequest request) {
        PageResult<SRS> result = service.findAll(
                request.getPage(),
                request.getSize(),
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(result);
    }

    /**
     * Updates a resource by its identifier.
     *
     * @param id      the resource identifier
     * @param request the update request body
     * @return the updated resource detail wrapped in {@link ApiResponse}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(this, 'UPDATE')")
    public ApiResponse<DRS> update(@PathVariable String id, @Valid @RequestBody URQ request) {
        return ApiResponse.ok(service.update(idEncoder.decode(id), request));
    }

    /**
     * Deletes a resource by its identifier.
     *
     * @param id the resource identifier
     * @return an empty success response
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(this, 'DELETE')")
    public ApiResponse<Void> delete(@PathVariable String id) {
        service.delete(idEncoder.decode(id));
        return ApiResponse.noContent();
    }
}
