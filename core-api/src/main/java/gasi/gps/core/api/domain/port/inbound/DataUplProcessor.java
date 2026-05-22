package gasi.gps.core.api.domain.port.inbound;

import java.util.List;
import java.util.Map;

import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.model.DataUpl;
import gasi.gps.core.api.file.FileRow;

/**
 * Resource-specific processor for shared upload workflows.
 *
 * <p>
 * Each uploadable resource contributes one processor bean. The shared
 * upload service owns persistence and workflow status changes, while
 * processors own parsing, row validation, and committing data to the target
 * resource.
 * </p>
 *
 * @since 1.0.0
 */
public interface DataUplProcessor {

    /**
     * Resource code handled by this processor.
     *
     * @return resource code used in upload API paths
     */
    String resource();

    /**
     * Parses an uploaded file into upload rows.
     *
     * @param rows    file rows to parse
     * @param dataUpl persisted upload header
     * @param params  additional parameters for parsing, e.g. from the API request
     * @return parsed rows
     */
    List<DataRowUpl> parse(List<FileRow> rows, DataUpl dataUpl, Map<String, String> params);

    /**
     * Validates upload rows.
     *
     * <p>
     * Processors should validate rows in batch so they can preload reference
     * data once, perform cross-row checks, and avoid one database query per row.
     * </p>
     *
     * @param dataUpl upload header
     * @param rows    rows to validate
     * @param params  additional parameters for validation, e.g. from the API
     *                request
     * @return validated rows
     */
    List<DataRowUpl> validateRows(DataUpl dataUpl, List<DataRowUpl> rows, Map<String, String> params);

    /**
     * Determines whether committing this upload should be routed to an external
     * approval flow instead of immediately writing to the target resource.
     *
     * <p>
     * The shared upload service only marks the upload as pending approval. The
     * approval request and post-approval commit are owned by the caller's
     * approval workflow.
     * </p>
     *
     * @param dataUpl upload header
     * @param rows    valid rows that would be committed
     * @param params  additional parameters for committing, e.g. from the API
     *                request
     * @return {@code true} when commit must wait for external approval
     */
    default boolean requiresApproval(DataUpl dataUpl, List<DataRowUpl> rows, Map<String, String> params) {
        return false;
    }

    /**
     * Commits valid rows to the target resource.
     *
     * @param dataUpl upload header
     * @param rows    valid rows to commit
     * @param params  additional parameters for committing, e.g. from the API
     *                request
     */
    void commitRows(DataUpl dataUpl, List<DataRowUpl> rows, Map<String, String> params);
}
