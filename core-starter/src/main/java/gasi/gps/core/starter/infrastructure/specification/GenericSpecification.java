package gasi.gps.core.starter.infrastructure.specification;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import gasi.gps.core.api.application.exception.BusinessException;
import gasi.gps.core.api.domain.model.AndFilter;
import gasi.gps.core.api.domain.model.GenericFilter;
import gasi.gps.core.api.domain.model.OrFilter;
import gasi.gps.core.api.domain.model.SimpleFilter;
import gasi.gps.core.starter.infrastructure.filter.FilterableFieldResolver;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Converts a {@link GenericFilter} tree into a JPA {@link Specification}.
 *
 * <p>Simple filter fields are resolved through {@link FilterableFieldResolver},
 * so only entity fields annotated with
 * {@link gasi.gps.core.starter.infrastructure.filter.Filterable} are accepted.</p>
 *
 * @param <T> entity type
 * @since 1.0.0
 */
public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    /** Filter expression converted by this specification. */
    private final GenericFilter filter;

    /**
     * Creates a specification for a filter expression.
     *
     * @param filter filter expression
     */
    public GenericSpecification(GenericFilter filter) {
        this.filter = filter;
    }

    /**
     * Creates a JPA specification for a filter expression.
     *
     * @param filter filter expression, or {@code null} to match all records
     * @param <T>    entity type
     * @return JPA specification
     */
    public static <T> Specification<T> from(GenericFilter filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return new GenericSpecification<>(filter);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return buildPredicate(filter, root, cb);
    }

    private Predicate buildPredicate(GenericFilter genericFilter, Root<T> root, CriteriaBuilder cb) {
        if (genericFilter instanceof SimpleFilter simpleFilter) {
            return buildSimplePredicate(simpleFilter, root, cb);
        } else if (genericFilter instanceof AndFilter andFilter) {
            Predicate[] predicates = andFilter.getFilters().stream()
                    .map(f -> buildPredicate(f, root, cb))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        } else if (genericFilter instanceof OrFilter orFilter) {
            Predicate[] predicates = orFilter.getFilters().stream()
                    .map(f -> buildPredicate(f, root, cb))
                    .toArray(Predicate[]::new);
            return cb.or(predicates);
        }
        throw new IllegalArgumentException("Unknown filter type: " + genericFilter.getClass().getName());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate buildSimplePredicate(SimpleFilter sf, Root<T> root, CriteriaBuilder cb) {
        if (sf.getField() == null || sf.getField().isBlank()) {
            throw new BusinessException("Filter 'field' is required");
        }
        if (sf.getOperator() == null) {
            throw new BusinessException("Filter 'operator' is required");
        }

        String field = FilterableFieldResolver.resolve(root.getJavaType(), sf.getField());
        Path<?> path = resolvePath(root, field);
        Object value = coerceValue(path, sf.getValue());

        return switch (sf.getOperator()) {
            case EQUALS -> cb.equal(path, value);
            case NOT_EQUALS -> cb.notEqual(path, value);
            case GREATER_THAN -> cb.greaterThan((Path<? extends Comparable>) path, (Comparable) value);
            case GREATER_THAN_OR_EQUALS -> cb.greaterThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value);
            case LESS_THAN -> cb.lessThan((Path<? extends Comparable>) path, (Comparable) value);
            case LESS_THAN_OR_EQUALS -> cb.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) value);
            case LIKE -> cb.like(path.as(String.class), "%" + value + "%");
            case IN -> path.in((Collection<?>) value);
            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object coerceValue(Path<?> path, Object value) {
        if (value == null) {
            return null;
        }

        Class<?> javaType = path.getJavaType();
        if (!javaType.isEnum()) {
            return value;
        }

        if (javaType.isInstance(value)) {
            return value;
        }

        if (value instanceof String text) {
            return Enum.valueOf((Class<? extends Enum>) javaType.asSubclass(Enum.class), text);
        }

        if (value instanceof Number number) {
            Object[] constants = javaType.getEnumConstants();
            int ordinal = number.intValue();
            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
        }

        return value;
    }

    private Path<?> resolvePath(Root<T> root, String field) {
        Path<?> path = root;
        for (String part : field.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }
}
