package com.example.core.classloader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CompositeClassLoader}.
 *
 * <p>
 * Verifies class loading delegation, resource lookup, and multi-resource
 * aggregation across parent and delegate classloaders.
 * </p>
 */
class CompositeClassLoaderTest {

    private ClassLoader parentClassLoader;

    @BeforeEach
    void setUp() {
        parentClassLoader = Thread.currentThread().getContextClassLoader();
    }

    // -----------------------------------------------------------------------
    // findClass
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("findClass")
    class FindClassTests {

        @Test
        @DisplayName("should load class from delegate when not in parent")
        void shouldLoadClassFromDelegate() throws ClassNotFoundException {
            // Use a non-bootstrap class that only the app classloader can find
            String targetClass = CompositeClassLoader.class.getName();
            ClassLoader appCl = CompositeClassLoader.class.getClassLoader();
            // EmptyClassLoader (no parent) can't find app classes,
            // so findClass will be called and delegate (appCl) will resolve it
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of(appCl));

            Class<?> loaded = composite.loadClass(targetClass);

            assertThat(loaded).isEqualTo(CompositeClassLoader.class);
        }

        @Test
        @DisplayName("should load class from parent classloader first")
        void shouldPreferParentClassLoader() throws ClassNotFoundException {
            var composite = new CompositeClassLoader(parentClassLoader, List.of());

            // Parent can always load standard JDK classes
            Class<?> loaded = composite.loadClass("java.util.List");

            assertThat(loaded).isEqualTo(java.util.List.class);
        }

        @Test
        @DisplayName("should throw ClassNotFoundException when class not found anywhere")
        void shouldThrowWhenClassNotFound() {
            var failingDelegate = new SingleClassLoader("java.lang.String");
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of(failingDelegate));

            // "FakeClass" won't match the delegate's className → triggers catch + throw
            assertThatThrownBy(() -> composite.loadClass("com.nonexistent.FakeClass"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("should try multiple delegates in order")
        void shouldTryDelegatesInOrder() throws ClassNotFoundException {
            var emptyDelegate = new EmptyClassLoader();
            var realDelegate = new SingleClassLoader("java.lang.Thread");

            var composite = new CompositeClassLoader(
                    new EmptyClassLoader(), // parent that can't find anything except bootstrap
                    List.of(emptyDelegate, realDelegate));

            Class<?> loaded = composite.loadClass("java.lang.Thread");

            assertThat(loaded).isEqualTo(Thread.class);
        }
    }

    // -----------------------------------------------------------------------
    // findResource
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("findResource")
    class FindResourceTests {

        @Test
        @DisplayName("should find resource from delegate")
        void shouldFindResourceFromDelegate() {
            URL fakeUrl = createFakeUrl("delegate-resource");
            var delegate = new SingleResourceClassLoader("test.txt", fakeUrl);
            // Use EmptyClassLoader so findResource is actually called
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of(delegate));

            URL result = composite.getResource("test.txt");

            assertThat(result).isEqualTo(fakeUrl);
        }

        @Test
        @DisplayName("should return null when resource not found")
        void shouldReturnNullWhenNotFound() {
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of());

            URL result = composite.getResource("nonexistent.txt");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should skip delegate that does not have the resource")
        void shouldSkipDelegateWithoutResource() {
            URL fakeUrl = createFakeUrl("found-resource");
            // First delegate has "other.txt", second has "test.txt"
            var delegate1 = new SingleResourceClassLoader("other.txt", createFakeUrl("other"));
            var delegate2 = new SingleResourceClassLoader("test.txt", fakeUrl);
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of(delegate1, delegate2));

            URL result = composite.getResource("test.txt");

            assertThat(result).isEqualTo(fakeUrl);
        }
    }

    // -----------------------------------------------------------------------
    // findResources
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("findResources")
    class FindResourcesTests {

        @Test
        @DisplayName("should aggregate resources from multiple delegates")
        void shouldAggregateFromMultipleDelegates() throws IOException {
            URL url1 = createFakeUrl("resource1");
            URL url2 = createFakeUrl("resource2");

            var delegate1 = new SingleResourceClassLoader("test.txt", url1);
            var delegate2 = new SingleResourceClassLoader("test.txt", url2);

            // Use an empty parent so only delegates contribute
            var composite = new CompositeClassLoader(
                    new EmptyClassLoader(),
                    List.of(delegate1, delegate2));

            Enumeration<URL> resources = composite.getResources("test.txt");
            List<URL> urls = Collections.list(resources);

            assertThat(urls).containsExactly(url1, url2);
        }

        @Test
        @DisplayName("should eliminate duplicate URLs")
        void shouldEliminateDuplicates() throws IOException {
            URL sameUrl = createFakeUrl("shared-resource");

            var delegate1 = new SingleResourceClassLoader("test.txt", sameUrl);
            var delegate2 = new SingleResourceClassLoader("test.txt", sameUrl);

            var composite = new CompositeClassLoader(
                    new EmptyClassLoader(),
                    List.of(delegate1, delegate2));

            Enumeration<URL> resources = composite.getResources("test.txt");
            List<URL> urls = Collections.list(resources);

            assertThat(urls).hasSize(1).containsExactly(sameUrl);
        }

        @Test
        @DisplayName("should return empty enumeration when no resources found")
        void shouldReturnEmptyWhenNoResources() throws IOException {
            var composite = new CompositeClassLoader(new EmptyClassLoader(), List.of());

            Enumeration<URL> resources = composite.getResources("nonexistent.txt");

            assertThat(Collections.list(resources)).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Test helpers
    // -----------------------------------------------------------------------

    /**
     * A classloader that can only load a single named class (from bootstrap).
     */
    private static class SingleClassLoader extends ClassLoader {
        private final String className;

        SingleClassLoader(String className) {
            super(null); // no parent
            this.className = className;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (className.equals(name)) {
                return Class.forName(name);
            }
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * A classloader that cannot find any class (except bootstrap classes).
     */
    private static class EmptyClassLoader extends ClassLoader {
        EmptyClassLoader() {
            super(null);
        }
    }

    /**
     * A classloader that returns a specific URL for a specific resource name.
     */
    private static class SingleResourceClassLoader extends ClassLoader {
        private final String resourceName;
        private final URL resourceUrl;

        SingleResourceClassLoader(String resourceName, URL resourceUrl) {
            super(null);
            this.resourceName = resourceName;
            this.resourceUrl = resourceUrl;
        }

        @Override
        public URL getResource(String name) {
            return resourceName.equals(name) ? resourceUrl : null;
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            if (resourceName.equals(name)) {
                return Collections.enumeration(List.of(resourceUrl));
            }
            return Collections.emptyEnumeration();
        }
    }

    private static URL createFakeUrl(String id) {
        try {
            return new URL("file:///fake/" + id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
