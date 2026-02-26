package gasi.gps.api.shared.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base for all filter types.
 * Subclasses: {@link SimpleFilter}, {@link AndFilter}, {@link OrFilter}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleFilter.class, name = "simple"),
        @JsonSubTypes.Type(value = AndFilter.class, name = "and"),
        @JsonSubTypes.Type(value = OrFilter.class, name = "or")
})
public abstract class GenericFilter {
}
