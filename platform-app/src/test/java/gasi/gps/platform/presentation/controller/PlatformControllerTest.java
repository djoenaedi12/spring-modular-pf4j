package gasi.gps.platform.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import gasi.gps.core.api.extension.AppExtension;
import gasi.gps.platform.bootstrap.PluginMetadataRegistry;

/**
 * Unit tests for {@link PlatformController}.
 *
 * <p>
 * Uses Mockito to mock {@link PluginManager} and {@link PluginWrapper}
 * to test the health endpoint without starting a Spring context.
 * </p>
 */
class PlatformControllerTest {

    private PluginManager pluginManager;
    private PlatformController controller;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        controller = new PlatformController(pluginManager, new PluginMetadataRegistry(pluginManager));
    }

    @Test
    @DisplayName("health should return UP status and app name")
    void healthShouldReturnUpStatus() {
        when(pluginManager.getStartedPlugins()).thenReturn(List.of());
        when(pluginManager.getExtensions(AppExtension.class)).thenReturn(List.of());

        Map<String, Object> result = controller.health();

        assertThat(result)
                .containsEntry("status", "UP")
                .containsEntry("app", "platform-app");
    }

    @Test
    @DisplayName("health should return empty plugin list when no plugins loaded")
    @SuppressWarnings("unchecked")
    void healthShouldReturnEmptyPluginList() {
        when(pluginManager.getStartedPlugins()).thenReturn(List.of());
        when(pluginManager.getExtensions(AppExtension.class)).thenReturn(List.of());

        Map<String, Object> result = controller.health();

        List<Map<String, String>> plugins = (List<Map<String, String>>) result.get("plugins");
        assertThat(plugins).isEmpty();
    }

    @Test
    @DisplayName("health should include plugin details when plugins are loaded")
    @SuppressWarnings("unchecked")
    void healthShouldIncludePluginDetails() {
        PluginWrapper pluginWrapper = mockPlugin("inventory-plugin", "1.0.0", PluginState.STARTED);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(pluginWrapper));
        when(pluginManager.getExtensions(AppExtension.class)).thenReturn(List.of());

        Map<String, Object> result = controller.health();

        List<Map<String, String>> plugins = (List<Map<String, String>>) result.get("plugins");
        assertThat(plugins).hasSize(1);
        assertThat(plugins.get(0))
                .containsEntry("id", "inventory-plugin")
                .containsEntry("version", "1.0.0")
                .containsEntry("state", "STARTED");
    }

    @Test
    @DisplayName("health should list multiple plugins")
    @SuppressWarnings("unchecked")
    void healthShouldListMultiplePlugins() {
        PluginWrapper inv = mockPlugin("inventory-plugin", "1.0.0", PluginState.STARTED);
        PluginWrapper ord = mockPlugin("order-plugin", "2.0.0", PluginState.STARTED);
        when(pluginManager.getStartedPlugins()).thenReturn(List.of(inv, ord));
        when(pluginManager.getExtensions(AppExtension.class)).thenReturn(List.of());

        Map<String, Object> result = controller.health();

        List<Map<String, String>> plugins = (List<Map<String, String>>) result.get("plugins");
        assertThat(plugins).hasSize(2);
        assertThat(plugins.get(0)).containsEntry("id", "inventory-plugin");
        assertThat(plugins.get(1)).containsEntry("id", "order-plugin");
    }

    @Test
    @DisplayName("health should include plugin module metadata")
    @SuppressWarnings("unchecked")
    void healthShouldIncludePluginModuleMetadata() {
        AppExtension extension = mock(AppExtension.class);
        when(extension.getModuleName()).thenReturn("auth-plugin");
        when(extension.getModuleDescription()).thenReturn("Authentication module");
        when(extension.getModuleVersion()).thenReturn("1.0.0");
        when(extension.getBasePackages()).thenReturn(List.of("gasi.gps.auth"));
        when(pluginManager.getStartedPlugins()).thenReturn(List.of());
        when(pluginManager.getExtensions(AppExtension.class)).thenReturn(List.of(extension));

        Map<String, Object> result = controller.health();

        List<String> basePackages = (List<String>) result.get("pluginBasePackages");
        assertThat(basePackages).containsExactly("gasi.gps.auth");
        assertThat((List<?>) result.get("modules")).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private PluginWrapper mockPlugin(String id, String version, PluginState state) {
        PluginWrapper wrapper = mock(PluginWrapper.class);
        PluginDescriptor descriptor = mock(PluginDescriptor.class);

        when(wrapper.getPluginId()).thenReturn(id);
        when(wrapper.getDescriptor()).thenReturn(descriptor);
        when(wrapper.getPluginState()).thenReturn(state);
        when(descriptor.getVersion()).thenReturn(version);

        return wrapper;
    }
}
