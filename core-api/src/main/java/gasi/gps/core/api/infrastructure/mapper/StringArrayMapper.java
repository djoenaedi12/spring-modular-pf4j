package gasi.gps.core.api.infrastructure.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class StringArrayMapper {

    @Named("stringToArray")
    public String[] stringToArray(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return value.split(",");
    }

    @Named("arrayToString")
    public String arrayToString(String[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return String.join(",", value);
    }
}
