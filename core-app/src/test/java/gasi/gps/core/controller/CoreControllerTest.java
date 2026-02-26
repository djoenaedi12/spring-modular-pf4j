package gasi.gps.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CoreController}.
 *
 * <p>
 * Uses Mockito to mock {@link PluginManager} and {@link PluginWrapper}
 * to test the health endpoint without starting a Spring context.
 * </p>
 */
class CoreControllerTest {

    private PluginManager pluginManager;
    private CoreController controller;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        controller = new CoreController(pluginManager);
    }

    @Test
    @DisplayName("health should return UP status and app name")
    void healthShouldReturnUpStatus() {
        when(pluginManager.getStartedPlugins()).thenReturn(List.of());

        Map<String, Object> result = controller.health();

        assertThat(result)
                .containsEntry("status", "UP")
                .containsEntry("app", "core-app");
    }

    @Test
    @DisplayName("health should return empty plugin list when no plugins loaded")
    @SuppressWarnings("unchecked")
    void healthShouldReturnEmptyPluginList() {
        when(pluginManager.getStartedPlugins()).thenReturn(List.of());

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

        Map<String, Object> result = controller.health();

        List<Map<String, String>> plugins = (List<Map<String, String>>) result.get("plugins");
        assertThat(plugins).hasSize(2);
        assertThat(plugins.get(0)).containsEntry("id", "inventory-plugin");
        assertThat(plugins.get(1)).containsEntry("id", "order-plugin");
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
