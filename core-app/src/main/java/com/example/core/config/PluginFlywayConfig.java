package com.example.core.config;

import com.example.api.FlywayMigrationExtension;
import org.flywaydb.core.Flyway;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Menjalankan Flyway migration untuk core-app DAN semua plugin yang
 * mengimplementasikan {@link FlywayMigrationExtension}.
 *
 * Strategi single table + version range:
 * - Core : V1 – V999 (lokasi: classpath:db/migration/core)
 * - Inventory: V1001 – V1999 (lokasi: classpath:db/migration/inventory)
 * - Product : V2001 – V2999 (lokasi: classpath:db/migration/product)
 *
 * Semua migration dijalankan dalam SATU Flyway instance dengan lokasi gabungan,
 * menggunakan satu tabel "flyway_schema_history".
 */
@Configuration
public class PluginFlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(PluginFlywayConfig.class);

    /**
     * Order(1) agar migration selesai sebelum Hibernate validate schema.
     * Hibernate validate dijalankan setelah context refresh, sedangkan
     * CommandLineRunner dijalankan setelahnya — tapi karena ddl-auto=validate
     * terjadi saat konteks start, kita perlu Flyway jalan lebih awal.
     *
     * Solusi: kita memastikan migration dijalankan SEBELUM EntityManagerFactory
     * dengan cara manual Flyway di sini dan DDL-auto=none sebagai alternatif,
     * atau pakai BeanFactoryPostProcessor. Untuk simplisitas, gunakan
     * ddl-auto=update
     * di standalone dan validate di core.
     */
    @Bean
    @Order(1)
    public CommandLineRunner runPluginMigrations(DataSource dataSource, PluginManager pluginManager) {
        return args -> {
            // Kumpulkan semua lokasi migration: core + semua plugin
            List<String> locations = new ArrayList<>();
            locations.add("classpath:db/migration/core");

            List<FlywayMigrationExtension> extensions = pluginManager.getExtensions(FlywayMigrationExtension.class);

            for (FlywayMigrationExtension ext : extensions) {
                String location = ext.getMigrationLocation();
                locations.add(location);
                log.info("[Flyway] Plugin migration location registered: {}", location);
            }

            log.info("[Flyway] Running migrations from {} location(s): {}", locations.size(), locations);

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations.toArray(new String[0]))
                    .table("flyway_schema_history") // satu tabel untuk semua
                    .mixed(true) // boleh campuran SQL + Java
                    .baselineOnMigrate(true) // handle DB yang sudah ada sebelum Flyway
                    .load();

            int applied = flyway.migrate().migrationsExecuted;
            log.info("[Flyway] Migration selesai. {} script dijalankan.", applied);
        };
    }
}
