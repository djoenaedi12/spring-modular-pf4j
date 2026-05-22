package gasi.gps.core.api.file;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper methods for upload row data snapshots and lookup values.
 *
 * @since 1.0.0
 */
public final class DataUplRowData {

    private DataUplRowData() {
    }

    /**
     * Returns the first non-blank value from the given keys.
     *
     * @param values source row values
     * @param keys   candidate keys in priority order
     * @return first non-blank value, or {@code null}
     */
    public static String firstValue(Map<String, String> values, String... keys) {
        if (values == null || keys == null) {
            return null;
        }

        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Serializes source row values to JSON, excluding metadata columns.
     *
     * @param values       source row values
     * @param excludedKeys keys to omit from rowData
     * @return JSON object string
     */
    public static String toRowData(Map<String, String> values, String... excludedKeys) {
        Map<String, String> rowData = new LinkedHashMap<>(values == null ? Map.of() : values);
        Set<String> exclusions = excludedKeys == null ? Set.of() : Set.of(excludedKeys);
        exclusions.forEach(rowData::remove);

        return rowData.entrySet().stream()
                .map(entry -> "\"" + escapeJson(entry.getKey()) + "\":\"" + escapeJson(entry.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
