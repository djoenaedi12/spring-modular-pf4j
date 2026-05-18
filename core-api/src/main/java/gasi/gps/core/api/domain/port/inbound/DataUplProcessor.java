package gasi.gps.core.api.domain.port.inbound;

import java.util.List;

import gasi.gps.core.api.domain.model.DataRowUpl;
import gasi.gps.core.api.domain.model.DataUplCommand;
import gasi.gps.core.api.domain.model.DataUpl;

/**
 * Resource-specific processor for shared upload workflows.
 *
 * <p>Each uploadable resource contributes one processor bean. The shared
 * upload service owns persistence and workflow status changes, while
 * processors own parsing, row validation, and committing data to the target
 * resource.</p>
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
     * @param upload uploaded file command
     * @param dataUpl persisted upload header
     * @return parsed rows
     */
    List<DataRowUpl> parse(DataUplCommand upload, DataUpl dataUpl);

    /**
     * Validates upload rows.
     *
     * <p>Processors should validate rows in batch so they can preload reference
     * data once, perform cross-row checks, and avoid one database query per row.</p>
     *
     * @param dataUpl upload header
     * @param rows    rows to validate
     * @return validated rows
     */
    List<DataRowUpl> validateRows(DataUpl dataUpl, List<DataRowUpl> rows);

    /**
     * Commits valid rows to the target resource.
     *
     * @param dataUpl upload header
     * @param rows    valid rows to commit
     */
    void commitRows(DataUpl dataUpl, List<DataRowUpl> rows);
}
