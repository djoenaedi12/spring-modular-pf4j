package com.example.inventory;

import java.util.List;

import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;

import com.example.api.AppExtension;
import com.example.inventory.controller.InventoryController;

@Extension
public class InventoryConfig implements AppExtension {

    @Bean
    public InventoryController pluginEndpoint() {
        System.out.println("booo");

        return new InventoryController();
    }

    @Override
    public String getModuleName() { return "Inventory"; }

    @Override
    public String getModuleDescription() { return "Inventory"; }

    @Override
    public List<Object> getControllers() { // TODO Auto-generated method stub
        return List.of(new InventoryController());
    }
}
