package gasi.gps.dataupload.domain.model;

import java.util.List;

/**
 * Column definition for an upload template.
 *
 * @param key       stable column key consumed by parsers
 * @param label     displayed column header
 * @param required  whether the value is mandatory
 * @param type      logical data type
 * @param comment   short header comment
 * @param guideline longer guideline text
 * @param options   static dropdown options
 * @param lookup    dynamic lookup/reference metadata
 */
public record DataUplTemplateColumn(
        String key,
        String label,
        boolean required,
        DataUplTemplateColumnType type,
        String comment,
        String guideline,
        List<DataUplTemplateOption> options,
        DataUplTemplateLookup lookup) {

    public DataUplTemplateColumn {
        type = type == null ? DataUplTemplateColumnType.TEXT : type;
        options = options == null ? List.of() : List.copyOf(options);
    }

    public static DataUplTemplateColumn text(String key, String label) {
        return text(key, label, false, null, null);
    }

    public static DataUplTemplateColumn text(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.TEXT, comment, guideline, List.of(), null);
    }

    public static DataUplTemplateColumn number(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.NUMBER, comment, guideline, List.of(), null);
    }

    public static DataUplTemplateColumn date(
            String key,
            String label,
            boolean required,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.DATE, comment, guideline, List.of(), null);
    }

    public static DataUplTemplateColumn options(
            String key,
            String label,
            boolean required,
            List<DataUplTemplateOption> options,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.ENUM, comment, guideline, options, null);
    }

    public static DataUplTemplateColumn lookup(
            String key,
            String label,
            boolean required,
            DataUplTemplateLookup lookup,
            String comment,
            String guideline) {
        return new DataUplTemplateColumn(
                key, label, required, DataUplTemplateColumnType.LOOKUP, comment, guideline, List.of(), lookup);
    }
}
