package gasi.gps.core.api.file;

import java.util.Map;

/**
 * One row parsed from a file.
 *
 * @param rowNumber source row number in the file
 * @param values    row values keyed by column name
 * @param rawData   original row representation for audit/debug usage
 * @since 1.0.0
 */
public record FileRow(
        int rowNumber,
        Map<String, String> values,
        String rawData) {
}
