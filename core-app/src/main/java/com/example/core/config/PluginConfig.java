package com.example.core.config;

import java.nio.file.Paths;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SpringPluginManager pluginManager() {
        // Points to the directory where your compiled plugin .jar files live
        // Default is usually "plugins", but you can customize it here
        return new SpringPluginManager(Paths.get("plugins"));
    }

    /*
     * @EventListener(ContextRefreshedEvent.class) public void
     * registerPluginEndpoints() { PluginManager pluginManager = new
     * DefaultPluginManager(); pluginManager.loadPlugins();
     * pluginManager.startPlugins();
     *
     * List<AppExtension> extensions =
     * pluginManager.getExtensions(AppExtension.class);
     *
     * GenericApplicationContext beanFactory = (GenericApplicationContext)
     * this.applicationContext;
     *
     * for (AppExtension ext : extensions) { // Register the plugin instance as a
     * singleton bean String beanName = ext.getClass().getName();
     *
     * // beanFactory.registerBean(beanName, null);
     *
     * beanFactory.registerBean(beanName, ext.getClass(),
     * (BeanDefinitionCustomizer[]) null);
     *
     * // beanFactory.registerBean(beanName, ext.getClass(), () -> ext);
     *
     * // Force Spring to re-scan for RequestMapping
     * this.applicationContext.getBeansOfType(RequestMappingHandlerMapping.class).
     * forEach((name, mapping) -> { // This logic varies slightly by Spring version,
     * // but usually involves calling detectHandlerMethods(ext) }); } }
     */
}
