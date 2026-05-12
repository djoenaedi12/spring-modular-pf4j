package gasi.gps.core.api.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base type for search filter expressions.
 *
 * <p>The JSON representation is polymorphic and selected by the {@code type}
 * property. Supported values are {@code simple}, {@code and}, and
 * {@code or}.</p>
 *
 * @see SimpleFilter
 * @see AndFilter
 * @see OrFilter
 * @since 1.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleFilter.class, name = "simple"),
        @JsonSubTypes.Type(value = AndFilter.class, name = "and"),
        @JsonSubTypes.Type(value = OrFilter.class, name = "or")
})
public abstract class GenericFilter implements Serializable {

    private static final long serialVersionUID = 1L;
}
