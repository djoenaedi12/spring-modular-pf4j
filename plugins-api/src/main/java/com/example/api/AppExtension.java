package com.example.api;

import java.util.List;

import org.pf4j.ExtensionPoint;

public interface AppExtension extends ExtensionPoint {
    // Info dasar yang harus diberikan setiap plugin ke Core
    String getModuleName();
    String getModuleDescription();

    List<Object> getControllers();
}
