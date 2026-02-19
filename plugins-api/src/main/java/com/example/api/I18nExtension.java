package com.example.api;

import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * Extension point untuk mendaftarkan message basenames i18n dari sebuah plugin.
 *
 * Core akan menggabungkan semua basenames dari plugin ke dalam satu
 * CompositeMessageSource, sehingga plugin bisa mendefinisikan key message
 * sendiri dan juga meng-override shared messages dari core.
 */
public interface I18nExtension extends ExtensionPoint {

    /**
     * Daftar basename message bundle yang dimiliki plugin ini.
     * Contoh: List.of("messages/inventory")
     *
     * File yang diharapkan ada di classpath plugin:
     * - messages/inventory.properties (default/EN)
     * - messages/inventory_id.properties (Bahasa Indonesia)
     */
    List<String> getMessageBasenames();
}
