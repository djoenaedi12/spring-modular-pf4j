package gasi.gps.dataupload.domain.port.inbound;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gasi.gps.dataupload.domain.model.DataRowUpl;
import gasi.gps.dataupload.domain.model.DataUpl;
import gasi.gps.dataupload.domain.model.DataUplTemplate;
import gasi.gps.dataupload.domain.model.DataUplTemplateColumn;
import gasi.gps.dataupload.domain.model.DataUplTemplateSpec;
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
     * Describes the upload template. The current renderer uses this for CSV
     * headers, while richer spreadsheet renderers can use comments, guidelines,
     * options, and lookup metadata later.
     *
     * @return upload template specification
     */
    default DataUplTemplateSpec templateSpec() {
        return DataUplTemplateSpec.builder(resource() + "-template.csv")
                .column(DataUplTemplateColumn.text("lookupValue1", "lookupValue1"))
                .column(DataUplTemplateColumn.text("lookupValue2", "lookupValue2"))
                .column(DataUplTemplateColumn.text("lookupValue3", "lookupValue3"))
                .build();
    }

    /**
     * Builds a downloadable template for this upload resource.
     *
     * @param params request parameters that may influence template generation
     * @return downloadable template content
     */
    default DataUplTemplate downloadTemplate(Map<String, String> params) {
        DataUplTemplateSpec spec = templateSpec();
        String csv = spec.columns().stream()
                .map(column -> firstNonBlank(column.label(), column.key()))
                .map(DataUplProcessor::csvCell)
                .collect(Collectors.joining(",")) + "\n";
        String fileName = firstNonBlank(spec.fileName(), resource() + "-template.csv");
        return new DataUplTemplate(fileName, "text/csv", csv.getBytes(StandardCharsets.UTF_8));
    }

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

    private static String csvCell(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.chars()
                .anyMatch(ch -> ch == ',' || ch == '"' || ch == '\n' || ch == '\r');
        if (!needsQuote) {
            return value;
        }
        return Arrays.stream(value.split("\"", -1))
                .collect(Collectors.joining("\"\"", "\"", "\""));
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
