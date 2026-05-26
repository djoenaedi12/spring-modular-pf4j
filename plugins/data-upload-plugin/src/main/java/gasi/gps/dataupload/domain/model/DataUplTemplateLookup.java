package gasi.gps.dataupload.domain.model;

import java.util.List;

/**
 * Lookup data used to create dropdown/reference sheets in richer templates.
 *
 * @param sheetName   suggested reference sheet name
 * @param headers     reference sheet headers
 * @param rows        reference rows
 * @param valueColumn column key used as submitted value
 * @param labelColumn column key used as human-readable label
 */
public record DataUplTemplateLookup(
        String sheetName,
        List<String> headers,
        List<List<String>> rows,
        String valueColumn,
        String labelColumn) {

    public DataUplTemplateLookup {
        headers = headers == null ? List.of() : List.copyOf(headers);
        rows = rows == null ? List.of() : rows.stream()
                .map(row -> row == null ? List.<String>of() : List.copyOf(row))
                .toList();
    }
}
