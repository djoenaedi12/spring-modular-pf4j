package com.example.order;

import com.example.api.AppExtension;
import com.example.api.FlywayMigrationExtension;
import com.example.api.I18nExtension;
import org.pf4j.Extension;

import java.util.List;

/**
 * Extension utama plugin-order.
 * Versi range Flyway: V2001 – V2999
 */
@Extension
public class OrderExtension implements AppExtension, FlywayMigrationExtension, I18nExtension {

    @Override
    public String getModuleName() {
        return "order-plugin";
    }

    @Override
    public String getModuleDescription() {
        return "Plugin untuk manajemen pesanan (order)";
    }

    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration/order";
    }

    @Override
    public List<String> getMessageBasenames() {
        return List.of("classpath:messages/order");
    }
}
