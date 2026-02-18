package com.example.inventory;

import org.pf4j.PluginWrapper;

import com.example.api.PluginModule;

/**
 * Class ini adalah entri utama untuk plugin. PF4J akan mencari class ini
 * berdasarkan konfigurasi di MANIFEST.MF (Plugin-Class)
 */
public class InventoryModule extends PluginModule {

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

    @Override
    public Object init(Object paramargs) {
        System.out.println("Initialized");

        return null;
    }
}