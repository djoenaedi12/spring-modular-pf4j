package com.example.core;

import java.util.List;

import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.api.PluginModule;

@SpringBootApplication(scanBasePackages = "com.example")
public class CoreApplication {

//    @Autowired
//    PluginManager pluginManager;

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    /**
     * Bean ini akan dijalankan otomatis setelah Spring Context siap. Kita
     * menggunakannya untuk memverifikasi plugin mana saja yang berhasil dimuat.
     */
    @Bean
    public CommandLineRunner checkPlugins(PluginManager pluginManager) {
        return args -> {
            System.out.println("");
            System.out.println("==============================================");
            System.out.println("   MODULAR SYSTEM STARTUP CHECK               ");
            System.out.println("==============================================");

            // PF4J-Spring secara otomatis memanggil loadPlugins() dan startPlugins()
            // saat ApplicationContext diinisialisasi, jadi kita tinggal ambil hasilnya.
            pluginManager.loadPlugins();

            List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();

            if (startedPlugins.isEmpty()) {
                System.out.println("STATUS: Tidak ada plugin yang aktif.");
                System.out.println("INFO  : Pastikan JAR ada di folder /plugins");
            }
            else {
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