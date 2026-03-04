package gasi.gps.core.api.infrastructure.mapper;

public interface StringArrayMapper {

    default String[] stringToArray(String value) {
        if (value == null || value.isBlank())
            return new String[0];
        return value.split(",");
    }

    default String arrayToString(String[] value) {
        if (value == null || value.length == 0)
            return null;
        return String.join(",", value);
    }
}
