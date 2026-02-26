package com.example.core.config;

import com.example.api.FlywayMigrationExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PluginFlywayConfig}.
 *
 * <p>
 * Verifies that the {@link CommandLineRunner} bean is correctly created
 * and that plugin migration locations are collected. Actual Flyway execution
 * is not tested here (that belongs in integration tests).
 * </p>
 */
class PluginFlywayConfigTest {

    private PluginManager pluginManager;
    private PluginFlywayConfig config;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        config = new PluginFlywayConfig();
    }

    @Test
    @DisplayName("should create CommandLineRunner bean")
    void shouldCreateBean() {
        when(pluginManager.getExtensions(FlywayMigrationExtension.class)).thenReturn(List.of());
        DataSource dataSource = mock(DataSource.class);

        CommandLineRunner runner = config.runPluginMigrations(dataSource, pluginManager);

        assertThat(runner).isNotNull();
    }

    @Test
    @DisplayName("should collect migration locations from plugins")
    void shouldCollectPluginMigrationLocations() throws Exception {
        FlywayMigrationExtension ext = mock(FlywayMigrationExtension.class);
        when(ext.getMigrationLocation()).thenReturn("classpath:db/migration/inventory");
        when(pluginManager.getExtensions(FlywayMigrationExtension.class))
                .thenReturn(List.of(ext));

        DataSource dataSource = createMockDataSource();

        CommandLineRunner runner = config.runPluginMigrations(dataSource, pluginManager);
        // Execute the runner — Flyway will run with the merged locations
        runner.run();

        // Verify plugin extension was queried
        verify(pluginManager).getExtensions(FlywayMigrationExtension.class);
        verify(ext).getMigrationLocation();
    }

    @Test
    @DisplayName("should collect multiple plugin migration locations")
    void shouldCollectMultiplePluginLocations() throws Exception {
        FlywayMigrationExtension invExt = mock(FlywayMigrationExtension.class);
        when(invExt.getMigrationLocation()).thenReturn("classpath:db/migration/inventory");

        FlywayMigrationExtension ordExt = mock(FlywayMigrationExtension.class);
        when(ordExt.getMigrationLocation()).thenReturn("classpath:db/migration/order");

        when(pluginManager.getExtensions(FlywayMigrationExtension.class))
                .thenReturn(List.of(invExt, ordExt));

        DataSource dataSource = createMockDataSource();

        CommandLineRunner runner = config.runPluginMigrations(dataSource, pluginManager);
        runner.run();

        verify(invExt).getMigrationLocation();
        verify(ordExt).getMigrationLocation();
    }

    @Test
    @DisplayName("should run with only core migrations when no plugins")
    void shouldRunWithCoreOnlyWhenNoPlugins() throws Exception {
        when(pluginManager.getExtensions(FlywayMigrationExtension.class))
                .thenReturn(List.of());

        DataSource dataSource = createMockDataSource();

        CommandLineRunner runner = config.runPluginMigrations(dataSource, pluginManager);
        runner.run();

        verify(pluginManager).getExtensions(FlywayMigrationExtension.class);
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Creates a mock DataSource backed by H2 in-memory database
     * so Flyway can actually execute.
     */
    private DataSource createMockDataSource() throws Exception {
        org.h2.jdbcx.JdbcDataSource h2 = new org.h2.jdbcx.JdbcDataSource();
        h2.setURL("jdbc:h2:mem:testdb_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        return h2;
    }
}
