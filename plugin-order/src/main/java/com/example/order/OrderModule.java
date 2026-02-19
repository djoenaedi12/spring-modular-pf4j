package com.example.order;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class OrderModule extends Plugin {

    public OrderModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println(">>> Order Module: Memulai plugin...");
    }

    @Override
    public void stop() {
        System.out.println(">>> Order Module: Menghentikan plugin...");
    }
}
