package gasi.gps.dataupload.domain.model;

/**
 * Static selectable option for an upload template column.
 *
 * @param value       submitted value
 * @param label       display label
 * @param description optional helper text
 */
public record DataUplTemplateOption(String value, String label, String description) {

    public static DataUplTemplateOption of(String value, String label) {
        return new DataUplTemplateOption(value, label, null);
    }
}
