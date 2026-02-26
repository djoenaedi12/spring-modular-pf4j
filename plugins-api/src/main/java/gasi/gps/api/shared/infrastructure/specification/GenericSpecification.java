package gasi.gps.api.shared.infrastructure.specification;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import gasi.gps.api.shared.domain.model.AndFilter;
import gasi.gps.api.shared.domain.model.GenericFilter;
import gasi.gps.api.shared.domain.model.OrFilter;
import gasi.gps.api.shared.domain.model.SimpleFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Converts a {@link GenericFilter} tree into a JPA {@link Specification}.
 *
 * @param <T> entity type
 */
public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    private final GenericFilter filter;

    public GenericSpecification(GenericFilter filter) {
        this.filter = filter;
    }

    /**
     * Convenience factory method.
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
        String field = sf.getField();
        Object value = sf.getValue();

        return switch (sf.getOperator()) {
            case EQUALS -> cb.equal(root.get(field), value);
            case NOT_EQUALS -> cb.notEqual(root.get(field), value);
            case GREATER_THAN -> cb.greaterThan(root.get(field), (Comparable) value);
            case GREATER_THAN_OR_EQUALS -> cb.greaterThanOrEqualTo(root.get(field), (Comparable) value);
            case LESS_THAN -> cb.lessThan(root.get(field), (Comparable) value);
            case LESS_THAN_OR_EQUALS -> cb.lessThanOrEqualTo(root.get(field), (Comparable) value);
            case LIKE -> cb.like(root.get(field), "%" + value + "%");
            case IN -> root.get(field).in((Collection<?>) value);
            case IS_NULL -> cb.isNull(root.get(field));
            case IS_NOT_NULL -> cb.isNotNull(root.get(field));
        };
    }
}
