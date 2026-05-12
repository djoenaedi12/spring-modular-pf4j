package gasi.gps.core.starter.infrastructure.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * MapStruct helper for comma-separated string and string-array conversion.
 *
 * @since 1.0.0
 */
@Component
public class StringArrayMapper {

    /**
     * Creates a string-array mapper.
     */
    public StringArrayMapper() {
    }

    /**
     * Splits a comma-separated string into an array.
     *
     * @param value comma-separated string
     * @return array of values, or an empty array when the input is blank
     */
    @Named("stringToArray")
    public String[] stringToArray(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return value.split(",");
    }

    /**
     * Joins a string array into a comma-separated string.
     *
     * @param value array of values
     * @return comma-separated string, or {@code null} when the input is empty
     */
    @Named("arrayToString")
    public String arrayToString(String[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return String.join(",", value);
    }
}
