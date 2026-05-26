package gasi.gps.dataupload.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource-specific upload template definition.
 *
 * @param fileName           suggested downloaded file name
 * @param mainSheetName      main data sheet name for future spreadsheet renderers
 * @param guidelineSheetName guideline sheet name for future spreadsheet renderers
 * @param columns            ordered data columns
 */
public record DataUplTemplateSpec(
        String fileName,
        String mainSheetName,
        String guidelineSheetName,
        List<DataUplTemplateColumn> columns) {

    public DataUplTemplateSpec {
        mainSheetName = mainSheetName == null || mainSheetName.isBlank() ? "Data" : mainSheetName;
        guidelineSheetName = guidelineSheetName == null || guidelineSheetName.isBlank()
                ? "Guideline"
                : guidelineSheetName;
        columns = columns == null ? List.of() : List.copyOf(columns);
    }

    public static Builder builder(String fileName) {
        return new Builder(fileName);
    }

    public static final class Builder {
        private final String fileName;
        private String mainSheetName = "Data";
        private String guidelineSheetName = "Guideline";
        private final List<DataUplTemplateColumn> columns = new ArrayList<>();

        private Builder(String fileName) {
            this.fileName = fileName;
        }

        public Builder mainSheetName(String value) {
            this.mainSheetName = value;
            return this;
        }

        public Builder guidelineSheetName(String value) {
            this.guidelineSheetName = value;
            return this;
        }

        public Builder column(DataUplTemplateColumn column) {
            this.columns.add(column);
            return this;
        }

        public Builder columns(List<DataUplTemplateColumn> values) {
            this.columns.addAll(values);
            return this;
        }

        public DataUplTemplateSpec build() {
            return new DataUplTemplateSpec(fileName, mainSheetName, guidelineSheetName, columns);
        }
    }
}
