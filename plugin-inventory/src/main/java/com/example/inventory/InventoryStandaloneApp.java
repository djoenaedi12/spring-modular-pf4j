package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class untuk menjalankan plugin-inventory secara standalone.
 * Gunakan Maven profile 'standalone' untuk menjalankan:
 *
 * mvn spring-boot:run -Pstandalone
 *
 * Class ini TIDAK digunakan saat plugin di-deploy ke core-app sebagai PF4J
 * plugin.
 */
@SpringBootApplication
public class InventoryStandaloneApp {

    public static void main(String[] args) {
        SpringApplication.run(InventoryStandaloneApp.class, args);
    }
}
