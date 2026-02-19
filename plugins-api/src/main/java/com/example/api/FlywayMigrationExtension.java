package com.example.api;

import org.pf4j.ExtensionPoint;

/**
 * Extension point untuk mendaftarkan lokasi Flyway migration dari sebuah
 * plugin.
 *
 * Konvensi version range:
 * - core-app : V1 – V999
 * - inventory : V1001 – V1999
 * - product : V2001 – V2999
 * - (plugin baru tambahkan range baru di sini)
 */
public interface FlywayMigrationExtension extends ExtensionPoint {

    /**
     * Lokasi script SQL migration, relatif terhadap classpath plugin.
     * Contoh: "classpath:db/migration/inventory"
     */
    String getMigrationLocation();
}
