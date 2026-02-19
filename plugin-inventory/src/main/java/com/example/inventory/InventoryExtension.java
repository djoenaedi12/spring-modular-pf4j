package com.example.inventory;

import com.example.api.AppExtension;
import com.example.api.FlywayMigrationExtension;
import org.pf4j.Extension;

/**
 * Extension utama plugin inventory.
 * Mendaftarkan:
 * - Info module (AppExtension)
 * - Lokasi Flyway migration (FlywayMigrationExtension) — range V1001–V1999
 * - Basename i18n (I18nExtension)
 */
@Extension
public class InventoryExtension implements AppExtension, FlywayMigrationExtension {

    @Override
    public String getModuleName() {
        return "inventory-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Plugin untuk manajemen inventaris barang";
    }

    /**
     * Lokasi SQL migration plugin ini.
     * Gunakan version range V1001–V1999 agar tidak konflik dengan plugin lain.
     */
    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration/inventory";
    }
}
