package com.example.core.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * ClassLoader komposit yang mendelegasikan pencarian class dan resource
 * ke beberapa classloader (dari plugin PF4J).
 * <p>
 * Flow: parent classloader dicoba dulu (Spring Boot app classloader),
 * lalu fallback ke plugin classloaders.
 */
public class CompositeClassLoader extends ClassLoader {

    private final List<ClassLoader> delegates;

    public CompositeClassLoader(ClassLoader parent, List<ClassLoader> delegates) {
        super(parent);
        this.delegates = new ArrayList<>(delegates);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader delegate : delegates) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                // coba delegate berikutnya
            }
        }
        throw new ClassNotFoundException(name);
    }

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
