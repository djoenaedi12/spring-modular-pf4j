package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point untuk menjalankan plugin-order secara standalone (development).
 * Aktifkan dengan: mvn spring-boot:run -Pstandalone
 *
 * CATATAN: Saat standalone, InventoryCheckExtension tidak tersedia
 * karena tidak ada plugin-inventory. OrderService akan skip pengecekan stok.
 */
@SpringBootApplication
public class OrderStandaloneApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderStandaloneApp.class, args);
    }
}
