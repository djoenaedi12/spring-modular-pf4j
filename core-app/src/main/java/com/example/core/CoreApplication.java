package com.example.core;

import com.example.api.PluginModule;
import com.example.core.classloader.CompositeClassLoader;
import org.pf4j.DefaultPluginManager;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@EntityScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
@SpringBootApplication(scanBasePackages = "com.example")
public class CoreApplication {

    // @Autowired
    // PluginManager pluginManager;

    public static void main(String[] args) {
        // ===== LANGKAH 1: Load plugin SEBELUM Spring ===========================
        // Buat PluginManager dulu, load dan start plugin.
        // Ini diperlukan agar kita bisa mendapatkan classloader dari setiap plugin.
        DefaultPluginManager pluginManager = new DefaultPluginManager(Paths.get("plugins"));
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        // ===== LANGKAH 2: Buat CompositeClassLoader ===========================
        // Gabungkan app classloader + semua plugin classloader, agar Spring Boot
        // component scan bisa menemukan class Controller, Repository, Entity dari
        // plugin.
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        List<ClassLoader> pluginClassLoaders = new ArrayList<>();
        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            pluginClassLoaders.add(plugin.getPluginClassLoader());
        }
        CompositeClassLoader compositeClassLoader = new CompositeClassLoader(appClassLoader, pluginClassLoaders);

        // Set sebagai context classloader agar semua framework (Spring, Hibernate)
        // menggunakan composite classloader
        Thread.currentThread().setContextClassLoader(compositeClassLoader);

        // ===== LANGKAH 3: Start Spring Boot dengan composite classloader ======
        SpringApplication app = new SpringApplication(
                new DefaultResourceLoader(compositeClassLoader),
                CoreApplication.class);

        // Register PluginManager sebagai singleton bean agar bisa di-inject
        // oleh beans lain (CoreController, CommandLineRunner, dll)
        app.addInitializers(ctx -> {
            ctx.getBeanFactory().registerSingleton("pluginManager", pluginManager);
        });

        app.run(args);
    }

    /**
     * Bean ini akan dijalankan otomatis setelah Spring Context siap.
     * Kita menggunakannya untuk memverifikasi plugin mana saja yang berhasil
     * dimuat.
     */
    @Bean
    public CommandLineRunner checkPlugins(PluginManager pluginManager) {
        return args -> {
            System.out.println("");
            System.out.println("==============================================");
            System.out.println("   MODULAR SYSTEM STARTUP CHECK               ");
            System.out.println("==============================================");

            List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();

            if (startedPlugins.isEmpty()) {
                System.out.println("STATUS: Tidak ada plugin yang aktif.");
                System.out.println("INFO  : Pastikan JAR ada di folder /plugins");
            } else {
                System.out.println("STATUS: Berhasil memuat " + startedPlugins.size() + " plugin.");
                for (PluginWrapper plugin : startedPlugins) {

                    Plugin plug = plugin.getPlugin();

                    if (plug instanceof PluginModule pm) {
                        pm.init(":");
                    }

                    System.out.println("----------------------------------------------");
                    System.out.println("ID Plugin      : " + plugin.getPluginId());
                    System.out.println("Versi          : " + plugin.getDescriptor().getVersion());
                    System.out.println("Class Utama    : " + plugin.getDescriptor().getPluginClass());
                    System.out.println("Status         : " + plugin.getPluginState());
                }
            }
            System.out.println("==============================================");
            System.out.println("");
        };
    }
}