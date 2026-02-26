package com.example.core.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A composite {@link ClassLoader} that delegates class and resource lookups
 * to multiple child classloaders (typically from PF4J plugins).
 *
 * <p>
 * <strong>Resolution order:</strong>
 * </p>
 * <ol>
 * <li>Parent classloader (Spring Boot application classloader)</li>
 * <li>Delegate plugin classloaders, in registration order</li>
 * </ol>
 *
 * <p>
 * This enables Spring Boot's component scan, JPA entity scan, and other
 * framework features to discover classes packaged inside plugin JARs.
 * </p>
 *
 * @see ClassLoader
 * @since 1.0.0
 */
public class CompositeClassLoader extends ClassLoader {

    private final List<ClassLoader> delegates;

    /**
     * Creates a new composite classloader.
     *
     * @param parent    the parent classloader (typically the application
     *                  classloader)
     * @param delegates the list of plugin classloaders to delegate to
     */
    public CompositeClassLoader(ClassLoader parent, List<ClassLoader> delegates) {
        super(parent);
        this.delegates = new ArrayList<>(delegates);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Iterates through all delegate classloaders until the class is found.
     * Throws {@link ClassNotFoundException} if no delegate can load it.
     * </p>
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader delegate : delegates) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                // try next delegate
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Returns the first matching resource found across delegate classloaders,
     * or {@code null} if none is found.
     * </p>
     */
    @Override
    protected URL findResource(String name) {
        for (ClassLoader delegate : delegates) {
            URL url = delegate.getResource(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Aggregates resources from all delegate classloaders, preserving
     * insertion order and eliminating duplicates.
     * </p>
     */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Set<URL> urls = new LinkedHashSet<>();
        for (ClassLoader delegate : delegates) {
            Enumeration<URL> resources = delegate.getResources(name);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }
        }
        return Collections.enumeration(urls);
    }
}
