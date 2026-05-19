package gasi.gps.core.starter.presentation.support;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import gasi.gps.core.api.domain.model.PageResult;

/**
 * Utility for applying caller-requested response field projections.
 *
 * <p>Projection is intentionally applied after DTO mapping so callers can only
 * select fields already exposed by the response DTO. Unknown fields are ignored,
 * and {@code id} is always included for list/table identity.</p>
 */
public final class ResponseProjection {

    private static final String REQUIRED_ID_FIELD = "id";

    private ResponseProjection() {
    }

    /**
     * Applies field projection to a list of DTOs.
     *
     * @param rows   response rows
     * @param fields requested public DTO field names
     * @return original rows when fields are blank, otherwise projected maps
     */
    public static List<?> projectList(List<?> rows, List<String> fields) {
        if (!hasProjection(fields)) {
            return rows;
        }

        Set<String> selectedFields = selectedFields(fields);
        return rows.stream()
                .map((row) -> projectObject(row, selectedFields))
                .collect(Collectors.toList());
    }

    /**
     * Applies field projection to a page result while preserving page metadata.
     *
     * @param pageResult page result
     * @param fields     requested public DTO field names
     * @return original page when fields are blank, otherwise a projected page
     */
    public static PageResult<?> projectPage(PageResult<?> pageResult, List<String> fields) {
        if (!hasProjection(fields)) {
            return pageResult;
        }

        Set<String> selectedFields = selectedFields(fields);
        List<Object> content = pageResult.getContent().stream()
                .map((row) -> projectObject(row, selectedFields))
                .collect(Collectors.toList());

        return PageResult.builder()
                .content(content)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    private static boolean hasProjection(List<String> fields) {
        return fields != null && fields.stream().anyMatch((field) -> field != null && !field.isBlank());
    }

    private static Set<String> selectedFields(List<String> fields) {
        Set<String> selected = new LinkedHashSet<>();
        selected.add(REQUIRED_ID_FIELD);
        fields.stream()
                .filter((field) -> field != null && !field.isBlank())
                .map(String::trim)
                .filter((field) -> !"class".equals(field))
                .forEach(selected::add);
        return selected;
    }

    private static Map<String, Object> projectObject(Object source, Set<String> fields) {
        if (source == null) {
            return Map.of();
        }

        Map<String, Function<Object, Object>> getters = readableProperties(source.getClass());
        Map<String, Object> projected = new LinkedHashMap<>();

        for (String field : fields) {
            Function<Object, Object> getter = getters.get(field);
            if (getter != null) {
                projected.put(field, getter.apply(source));
            }
        }

        return projected;
    }

    private static Map<String, Function<Object, Object>> readableProperties(Class<?> type) {
        try {
            Map<String, Function<Object, Object>> getters = new LinkedHashMap<>();
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if (descriptor.getReadMethod() == null || "class".equals(descriptor.getName())) {
                    continue;
                }

                getters.put(descriptor.getName(), (source) -> invokeGetter(source, descriptor));
            }
            return getters;
        } catch (IntrospectionException ex) {
            return Map.of();
        }
    }

    private static Object invokeGetter(Object source, PropertyDescriptor descriptor) {
        try {
            return descriptor.getReadMethod().invoke(source);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return null;
        }
    }
}
