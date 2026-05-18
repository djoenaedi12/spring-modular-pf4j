package gasi.gps.core.starter.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import gasi.gps.core.api.domain.model.BaseModel;
import gasi.gps.core.api.domain.port.inbound.BaseService;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.starter.infrastructure.util.IdEncoder;
import jakarta.validation.Valid;

/**
 * Abstract REST controller for standard CRUD resource endpoints.
 *
 * <p>
 * This controller builds on {@link BaseReadController} for read and search
 * endpoints, then adds create, update, and delete endpoints for full CRUD
 * resources. Subclasses only need to supply the concrete {@link BaseService}
 * via the constructor and annotate with {@code @RestController} and
 * {@code @RequestMapping}.
 * </p>
 *
 * <h2>Exposed endpoints relative to the subclass mapping</h2>
 * <table>
 * <caption>Default CRUD resource endpoints</caption>
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
 * @param <CRQ> create request DTO type
 * @param <URQ> update request DTO type
 * @param <SRS> summary response DTO type (for lists)
 * @param <DRS> detail response DTO type (for single entity)
 * @since 1.0.0
 */
public abstract class BaseController<D extends BaseModel, CRQ, URQ, SRS, DRS>
        extends BaseReadController<SRS, DRS> {

    private final BaseService<D, CRQ, URQ, SRS, DRS> service;

    /**
     * Constructs a new {@code BaseController}.
     *
     * @param service   the service handling business logic
     * @param idEncoder the ID encoder for encoding/decoding IDs
     */
    protected BaseController(BaseService<D, CRQ, URQ, SRS, DRS> service,
            IdEncoder idEncoder) {
        super(service, idEncoder);
        this.service = service;
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
     * Updates a resource by its identifier.
     *
     * @param id      the resource identifier
     * @param request the update request body
     * @return the updated resource detail wrapped in {@link ApiResponse}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(this, 'UPDATE')")
    public ApiResponse<DRS> update(@PathVariable String id, @Valid @RequestBody URQ request) {
        return ApiResponse.ok(service.update(getIdEncoder().decode(id), request));
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
        service.delete(getIdEncoder().decode(id));
        return ApiResponse.noContent();
    }
}
