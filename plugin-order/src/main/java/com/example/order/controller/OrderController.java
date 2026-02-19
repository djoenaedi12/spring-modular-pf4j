package com.example.order.controller;

import com.example.api.InventoryCheckExtension;
import com.example.api.InventoryItemDTO;
import com.example.order.dto.OrderResponse;
import com.example.order.model.Order;
import com.example.order.service.OrderService;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PluginManager pluginManager;

    public OrderController(OrderService orderService,
                           @Autowired(required = false) PluginManager pluginManager) {
        this.orderService = orderService;
        this.pluginManager = pluginManager;
    }

    // =========================================================
    // INTERFACE APPROACH (Recommended)
    // Akses data plugin-inventory via InventoryCheckExtension
    // yang didefinisikan di plugins-api.
    // Tidak ada import langsung ke class dari plugin-inventory.
    // =========================================================

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(this::toResponseViaInterface)
                .toList();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody Map<String, Object> body) {
        Long itemId = Long.valueOf(body.get("itemId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        Order order = orderService.createOrder(itemId, quantity);
        OrderResponse response = toResponseViaInterface(order);

        return "REJECTED".equals(order.getStatus())
                ? ResponseEntity.badRequest().body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * INTERFACE APPROACH:
     * Ambil detail item via InventoryCheckExtension (interface di plugins-api).
     * plugin-order tidak tahu sama sekali tentang class InventoryItem dari plugin-inventory.
     * Kalau plugin-inventory tidak ada (standalone mode), item akan null.
     */
    private OrderResponse toResponseViaInterface(Order order) {
        InventoryItemDTO itemDTO = null;
        if (pluginManager != null) {
            List<InventoryCheckExtension> extensions =
                    pluginManager.getExtensions(InventoryCheckExtension.class);
            if (!extensions.isEmpty()) {
                itemDTO = extensions.get(0).getItemDetails(order.getInventoryItemId());
            }
        }
        return new OrderResponse(order.getId(), order.getStatus(), order.getQuantity(), itemDTO);
    }

    // =========================================================
    // DIRECT IMPORT APPROACH (Tightly Coupled — untuk referensi)
    //
    // Untuk menggunakan pendekatan ini, tambahkan di pom.xml:
    //   <dependency>
    //       <groupId>com.example</groupId>
    //       <artifactId>plugin-inventory</artifactId>
    //       <version>1.0.0</version>
    //       <scope>provided</scope>  ← provided di mode normal
    //   </dependency>
    //
    // Lalu bisa langsung import:
    //   import com.example.inventory.model.InventoryItem;
    //   import com.example.inventory.repository.InventoryRepository;
    //
    // Contoh penggunaan:
    //   @Autowired
    //   private InventoryRepository inventoryRepository; // dari plugin-inventory!
    //
    //   private OrderResponse toResponseDirect(Order order) {
    //       InventoryItem item = inventoryRepository.findById(order.getInventoryItemId())
    //               .orElse(null);
    //       ...
    //   }
    //
    // Konsekuensi:
    // - plugin-order coupling ke plugin-inventory secara langsung
    // - Standalone mode crash jika plugin-inventory tidak ada di classloader
    // - Lebih mudah karena tidak perlu interface, tapi tidak fleksibel
    // =========================================================
}
