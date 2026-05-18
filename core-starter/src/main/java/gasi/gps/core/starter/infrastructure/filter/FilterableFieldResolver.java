package gasi.gps.core.starter.infrastructure.filter;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gasi.gps.core.api.application.exception.BusinessException;

/**
 * Resolves public API filter and sort fields to JPA entity field names.
 *
 * <p>The resolver scans {@link Filterable} annotations on the entity class and
 * its superclasses, then caches the resolved mapping per entity class.</p>
 *
 * @since 1.0.0
 */
public final class FilterableFieldResolver {

    private static final Map<Class<?>, Map<String, String>> CACHE = new ConcurrentHashMap<>();
    private static final String PATH_SEPARATOR = ".";

    private FilterableFieldResolver() {
    }

    /**
     * Resolves a public filter/sort field to the matching entity field.
     *
     * @param entityClass the JPA entity class
     * @param publicField the field name received from the API
     * @return the actual Java field name on the entity
     * @throws BusinessException if the field is blank or not annotated as
     *                           filterable
     */
    public static String resolve(Class<?> entityClass, String publicField) {
        if (publicField == null || publicField.isBlank()) {
            throw new BusinessException("Filter field is required");
        }

        String entityField = resolvePath(entityClass, publicField);
        if (entityField == null) {
            throw new BusinessException("Filter field is not allowed: " + publicField);
        }
        return entityField;
    }

    private static String resolvePath(Class<?> entityClass, String publicField) {
        if (!publicField.contains(PATH_SEPARATOR)) {
            return filterableFields(entityClass).get(publicField);
        }

        String[] pathParts = publicField.split("\\.", -1);
        Class<?> currentClass = entityClass;
        StringBuilder resolvedPath = new StringBuilder();

        for (String pathPart : pathParts) {
            if (pathPart.isBlank()) {
                return null;
            }

            String resolvedPart = filterableFields(currentClass).get(pathPart);
            if (resolvedPart == null) {
                return null;
            }

            if (resolvedPath.length() > 0) {
                resolvedPath.append(PATH_SEPARATOR);
            }
            resolvedPath.append(resolvedPart);

            Field field = findField(currentClass, resolvedPart);
            if (field == null) {
                return null;
            }
            currentClass = field.getType();
        }

        return resolvedPath.toString();
    }

    private static Map<String, String> filterableFields(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, FilterableFieldResolver::scan);
    }

    private static Map<String, String> scan(Class<?> entityClass) {
        Map<String, String> fields = new LinkedHashMap<>();
        Class<?> current = entityClass;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Filterable filterable = field.getAnnotation(Filterable.class);
                if (filterable == null) {
                    continue;
                }

                String publicName = filterable.alias().isBlank()
                        ? field.getName()
                        : filterable.alias();

                String previous = fields.putIfAbsent(publicName, field.getName());
                if (previous != null && !previous.equals(field.getName())) {
                    throw new IllegalStateException("Duplicate filter alias '" + publicName
                            + "' on entity " + entityClass.getName());
                }
            }
            current = current.getSuperclass();
        }

        return Map.copyOf(fields);
    }

    private static Field findField(Class<?> entityClass, String fieldName) {
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
