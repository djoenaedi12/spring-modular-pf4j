package com.example.inventory;

import com.example.api.AppExtension;
import com.example.api.FlywayMigrationExtension;
import com.example.api.I18nExtension;
import org.pf4j.Extension;

import java.util.List;

/**
 * Extension utama plugin inventory.
 * Mendaftarkan:
 * - Info module (AppExtension)
 * - Lokasi Flyway migration (FlywayMigrationExtension) — range V1001–V1999
 * - Basename i18n (I18nExtension)
 */
@Extension
public class InventoryExtension implements AppExtension, FlywayMigrationExtension, I18nExtension {

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

    /**
     * Basename file message bundle plugin ini.
     * Konvensi: messages/<nama-plugin>.properties
     */
    @Override
    public List<String> getMessageBasenames() {
        return List.of("classpath:messages/inventory");
    }
}
