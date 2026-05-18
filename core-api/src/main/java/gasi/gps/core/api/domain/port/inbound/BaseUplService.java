package gasi.gps.core.api.domain.port.inbound;

import java.util.List;

import gasi.gps.core.api.application.dto.DataRowUplDetailResponse;
import gasi.gps.core.api.application.dto.DataRowUplSummaryResponse;
import gasi.gps.core.api.application.dto.DataUplDetailResponse;
import gasi.gps.core.api.application.dto.DataUplSummaryResponse;
import gasi.gps.core.api.domain.model.DataUplCommand;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.PageResult;
import gasi.gps.core.api.domain.model.SortOrder;

/**
 * Inbound service contract for shared data upload workflows.
 *
 * <p>Upload metadata and rows are stored in shared upload tables. The
 * {@code resource} parameter scopes every operation to the business resource
 * being uploaded, while resource-specific parsing, validation, and commit
 * behavior is delegated to upload processors.</p>
 *
 * @since 1.0.0
 */
public interface BaseUplService extends BaseReadService<DataUplSummaryResponse, DataUplDetailResponse> {

    /**
     * Uploads a file for the given resource.
     *
     * @param resource resource code from the API path
     * @param file     uploaded file command
     * @return detail response for the created upload
     */
    DataUplDetailResponse upload(String resource, DataUplCommand file);

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
     */
    void validate(String resource, String id);

    /**
     * Commits all valid rows for an upload.
     *
     * @param resource resource code from the API path
     * @param id       encoded upload identifier
     */
    void commit(String resource, String id);

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
