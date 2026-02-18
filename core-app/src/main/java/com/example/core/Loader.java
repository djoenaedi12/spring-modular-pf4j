package com.example.core;

import java.lang.reflect.Method;
import java.util.List;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.example.api.AppExtension;

@Component
public class Loader {
    private final SpringPluginManager          pluginManager;
    private final RequestMappingHandlerMapping handlerMapping;

    public Loader(SpringPluginManager pluginManager, RequestMappingHandlerMapping handlerMapping) {
        this.pluginManager  = pluginManager;
        this.handlerMapping = handlerMapping;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void registerPluginEndpoints() {
        List<AppExtension> extensions = this.pluginManager.getExtensions(AppExtension.class);

        for (AppExtension extension : extensions) {
            for (Object controller : extension.getControllers()) {
                registerController(controller);
            }
        }
    }

    private void registerController(Object controller) {
        // Use Reflection to detect @RequestMapping methods and register them
        Method method = ReflectionUtils.findMethod(this.handlerMapping.getClass(), "detectHandlerMethods", Object.class);
        method.setAccessible(true);
        ReflectionUtils.invokeMethod(method, this.handlerMapping, controller);
    }
}
