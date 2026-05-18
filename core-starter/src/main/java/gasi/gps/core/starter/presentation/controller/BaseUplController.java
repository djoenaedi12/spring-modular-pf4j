package gasi.gps.core.starter.presentation.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import gasi.gps.core.api.application.dto.DataRowUplDetailResponse;
import gasi.gps.core.api.application.dto.DataRowUplSummaryResponse;
import gasi.gps.core.api.application.dto.DataUplDetailResponse;
import gasi.gps.core.api.application.dto.DataUplSummaryResponse;
import gasi.gps.core.api.domain.model.DataUplInput;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.port.inbound.BaseUplService;
import gasi.gps.core.api.presentation.dto.ApiResponse;
import gasi.gps.core.api.presentation.dto.SearchRequest;

/**
 * Abstract REST controller for shared upload workflows.
 *
 * <p>
 * Every endpoint is scoped by a {@code resource} path variable so one
 * upload table can serve multiple business resources.
 * </p>
 *
 * @since 1.0.0
 */
public abstract class BaseUplController {

    private final BaseUplService service;

    /**
     * Creates a base upload controller.
     *
     * @param service upload workflow service
     */
    protected BaseUplController(BaseUplService service) {
        this.service = service;
    }

    /**
     * Uploads a file for a resource.
     *
     * @param resource resource code
     * @param file     uploaded file
     * @return created upload detail
     */
    @PostMapping
    @PreAuthorize("hasPermission(this, 'CREATE')")
    public ApiResponse<DataUplDetailResponse> upload(
            @PathVariable String resource,
            @RequestParam("file") MultipartFile file,
            @RequestParam Map<String, String> parameters) throws IOException {
        DataUplInput command = new DataUplInput(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                resource,
                Map.copyOf(parameters));
        return ApiResponse.ok(service.upload(resource, command));
    }

    /**
     * Retrieves an upload by ID.
     *
     * @param resource resource code
     * @param id       encoded upload identifier
     * @return upload detail
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DataUplDetailResponse> findById(
            @PathVariable String resource,
            @PathVariable String id) {
        return ApiResponse.ok(service.findById(resource, id));
    }

    /**
     * Finds uploads for a resource.
     *
     * @param resource resource code
     * @param request  search request
     * @return page of upload summaries
     */
    @PostMapping("/search/page")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<PageResult<DataUplSummaryResponse>> findAllPaged(
            @PathVariable String resource,
            @RequestBody SearchRequest request) {
        PageResult<DataUplSummaryResponse> result = service.findAll(
                resource,
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(result);
    }

    /**
     * Validates an upload.
     *
     * @param resource resource code
     * @param id       encoded upload identifier
     * @return empty success response
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasPermission(this, 'UPDATE')")
    public ApiResponse<Void> validate(
            @PathVariable String resource,
            @PathVariable String id,
            @RequestParam Map<String, String> parameters) {
        service.validate(resource, id, parameters);
        return ApiResponse.noContent();
    }

    /**
     * Commits an upload.
     *
     * @param resource resource code
     * @param id       encoded upload identifier
     * @return empty success response
     */
    @PostMapping("/{id}/commit")
    @PreAuthorize("hasPermission(this, 'UPDATE')")
    public ApiResponse<Void> commit(
            @PathVariable String resource,
            @PathVariable String id,
            @RequestParam Map<String, String> parameters) {
        service.commit(resource, id, parameters);
        return ApiResponse.noContent();
    }

    /**
     * Retrieves one upload row.
     *
     * @param resource resource code
     * @param uploadId encoded upload identifier
     * @param rowId    encoded row identifier
     * @return row detail
     */
    @GetMapping("/{uploadId}/rows/{rowId}")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<DataRowUplDetailResponse> findRowById(
            @PathVariable String resource,
            @PathVariable String uploadId,
            @PathVariable String rowId) {
        return ApiResponse.ok(service.findRowById(resource, uploadId, rowId));
    }

    /**
     * Finds rows for an upload.
     *
     * @param resource resource code
     * @param uploadId encoded upload identifier
     * @param request  search request
     * @return page of row summaries
     */
    @PostMapping("/{uploadId}/rows/search/page")
    @PreAuthorize("hasPermission(this, 'READ')")
    public ApiResponse<PageResult<DataRowUplSummaryResponse>> findRows(
            @PathVariable String resource,
            @PathVariable String uploadId,
            @RequestBody SearchRequest request) {
        PageResult<DataRowUplSummaryResponse> result = service.findRows(
                resource,
                uploadId,
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                request.getFilter(),
                request.getSorts() != null ? request.getSorts() : Collections.emptyList());
        return ApiResponse.ok(result);
    }
}
