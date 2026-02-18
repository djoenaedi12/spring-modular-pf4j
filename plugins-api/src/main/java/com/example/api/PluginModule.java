package com.example.api;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public abstract class PluginModule extends Plugin {
    public PluginModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    public abstract Object init(Object paramargs);
}
