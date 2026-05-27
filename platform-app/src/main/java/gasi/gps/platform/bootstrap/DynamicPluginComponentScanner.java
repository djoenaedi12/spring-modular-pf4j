package gasi.gps.platform.bootstrap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import gasi.gps.core.api.extension.AppExtension;

/**
 * Registers Spring components contributed by PF4J plugins at startup.
 */
@Component
public class DynamicPluginComponentScanner implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware, ResourceLoaderAware {

    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        PluginManager pluginManager = beanFactory.getBean(PluginManager.class);
        Set<String> basePackages = pluginBasePackages(pluginManager);

        if (basePackages.isEmpty()) {
            return;
        }

        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, true);
        scanner.setResourceLoader(resourceLoader);
        scanner.scan(basePackages.toArray(new String[0]));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op.
    }

    private Set<String> pluginBasePackages(PluginManager pluginManager) {
        Set<String> basePackages = new LinkedHashSet<>();

        for (AppExtension extension : pluginManager.getExtensions(AppExtension.class)) {
            basePackages.addAll(extension.getBasePackages());
        }

        for (PluginWrapper wrapper : pluginManager.getStartedPlugins()) {
            basePackages.add(wrapper.getPlugin().getClass().getPackageName());
        }

        return basePackages;
    }
}
