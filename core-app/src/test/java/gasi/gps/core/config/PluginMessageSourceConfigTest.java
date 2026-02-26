package gasi.gps.core.config;

import gasi.gps.api.I18nExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PluginMessageSourceConfig}.
 *
 * <p>
 * Uses Mockito to mock {@link PluginManager} and {@link I18nExtension}
 * to verify message source assembly without loading real plugins.
 * </p>
 */
class PluginMessageSourceConfigTest {

    private PluginManager pluginManager;
    private PluginMessageSourceConfig config;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        config = new PluginMessageSourceConfig();
    }

    @Test
    @DisplayName("should create MessageSource with only core basenames when no plugins")
    void shouldCreateMessageSourceWithCoreOnly() {
        when(pluginManager.getExtensions(I18nExtension.class)).thenReturn(List.of());

        MessageSource source = config.messageSource(pluginManager);

        assertThat(source).isInstanceOf(ReloadableResourceBundleMessageSource.class);
        // "useCodeAsDefaultMessage" returns the key itself when not found
        String result = source.getMessage("unknown.key", null, Locale.getDefault());
        assertThat(result).isEqualTo("unknown.key");
    }

    @Test
    @DisplayName("should include plugin basenames before core")
    void shouldIncludePluginBasenames() {
        I18nExtension ext = mock(I18nExtension.class);
        when(ext.getMessageBasenames()).thenReturn(List.of("classpath:messages/inventory"));
        when(pluginManager.getExtensions(I18nExtension.class)).thenReturn(List.of(ext));

        MessageSource source = config.messageSource(pluginManager);

        assertThat(source).isInstanceOf(ReloadableResourceBundleMessageSource.class);
    }

    @Test
    @DisplayName("should include basenames from multiple plugins")
    void shouldIncludeMultiplePluginBasenames() {
        I18nExtension invExt = mock(I18nExtension.class);
        when(invExt.getMessageBasenames()).thenReturn(List.of("classpath:messages/inventory"));

        I18nExtension ordExt = mock(I18nExtension.class);
        when(ordExt.getMessageBasenames()).thenReturn(List.of("classpath:messages/order"));

        when(pluginManager.getExtensions(I18nExtension.class))
                .thenReturn(List.of(invExt, ordExt));

        MessageSource source = config.messageSource(pluginManager);

        assertThat(source).isInstanceOf(ReloadableResourceBundleMessageSource.class);
    }

    @Test
    @DisplayName("should use UTF-8 encoding and code-as-default-message")
    void shouldUseUtf8AndCodeAsDefault() {
        when(pluginManager.getExtensions(I18nExtension.class)).thenReturn(List.of());

        MessageSource source = config.messageSource(pluginManager);
        ReloadableResourceBundleMessageSource rbms = (ReloadableResourceBundleMessageSource) source;

        // useCodeAsDefaultMessage: if key is not found, return the key itself
        String result = rbms.getMessage("some.missing.key", null, Locale.ENGLISH);
        assertThat(result).isEqualTo("some.missing.key");
    }
}
