package com.example.inventory;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * Class ini adalah entri utama untuk plugin.
 * PF4J akan mencari class ini berdasarkan konfigurasi di MANIFEST.MF
 * (Plugin-Class)
 */
public class InventoryModule extends Plugin {

    public InventoryModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println(">>> Inventory Module: Memulai plugin...");
    }

    @Override
    public void stop() {
        System.out.println(">>> Inventory Module: Menghentikan plugin...");
    }
}