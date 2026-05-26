package gasi.gps.dataupload.domain.port.inbound;

import java.util.List;
import java.util.Map;

import gasi.gps.dataupload.application.dto.DataRowUplDetailResponse;
import gasi.gps.dataupload.application.dto.DataRowUplSummaryResponse;
import gasi.gps.dataupload.application.dto.DataUplDetailResponse;
import gasi.gps.dataupload.application.dto.DataUplSummaryResponse;
import gasi.gps.dataupload.domain.model.DataUplTemplate;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;
import gasi.gps.core.api.domain.port.inbound.BaseReadService;
import gasi.gps.core.api.file.FileReadInput;

/**
 * Inbound service contract for shared data upload workflows.
 *
 * <p>
 * Upload metadata and rows are stored in shared upload tables. The
 * {@code resource} parameter scopes every operation to the business resource
 * being uploaded, while resource-specific parsing, validation, and commit
 * behavior is delegated to upload processors.
 * </p>
 *
 * @since 1.0.0
 */
public interface BaseUplService extends BaseReadService<DataUplSummaryResponse, DataUplDetailResponse> {

    /**
     * Uploads a file for the given resource.
     *
     * @param resource resource code from the API path
     * @param file     uploaded file input
     * @return detail response for the created upload
     */
    DataUplDetailResponse upload(String resource, FileReadInput file);

    /**
     * Downloads a resource-specific upload template.
     *
     * @param resource resource code from the API path
     * @param params   template parameters
     * @return template content
     */
    DataUplTemplate downloadTemplate(String resource, Map<String, String> params);

    /**
     * Finds an upload scoped by resource.
     *
     * @param resource resource code from the API path
     * @param id       encoded upload identifier
     * @return upload detail response
     */
    DataUplDetailResponse findById(String resource, String id);

    /**
     * Finds a page of uploads scoped by resource.
     *
     * @param resource resource code from the API path
     * @param page     zero-based page index
     * @param size     requested page size
     * @param filter   optional caller filter
     * @param orders   sort orders
     * @return page of upload summaries
     */
    PageResult<DataUplSummaryResponse> findAll(String resource, int page, int size,
            GenericFilter filter, List<SortOrder> orders);

    /**
     * Validates all rows for an upload.
     *
     * @param resource resource code from the API path
     * @param id       encoded upload identifier
     * @param params   validation parameters
     */
    void validate(String resource, String id, Map<String, String> params);

    /**
     * Commits all valid rows for an upload.
     *
     * @param resource resource code from the API path
     * @param id       encoded upload identifier
     * @param params   commit parameters
     */
    void commit(String resource, String id, Map<String, String> params);

    /**
     * Deletes an upload attempt that has not reached a protected processing or
     * committed state.
     *
     * @param resource resource code from the API path
     * @param id       encoded upload identifier
     */
    void delete(String resource, String id);

    /**
     * Finds one upload row scoped by resource and upload.
     *
     * @param resource resource code from the API path
     * @param uploadId encoded upload identifier
     * @param rowId    encoded row identifier
     * @return row detail response
     */
    DataRowUplDetailResponse findRowById(String resource, String uploadId, String rowId);

    /**
     * Finds upload rows scoped by resource and upload.
     *
     * @param resource resource code from the API path
     * @param uploadId encoded upload identifier
     * @param page     zero-based page index
     * @param size     requested page size
     * @param filter   optional caller filter
     * @param orders   sort orders
     * @return page of row summaries
     */
    PageResult<DataRowUplSummaryResponse> findRows(String resource, String uploadId, int page, int size,
            GenericFilter filter, List<SortOrder> orders);

}
