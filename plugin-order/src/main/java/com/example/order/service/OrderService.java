package com.example.order.service;

import com.example.api.InventoryCheckExtension;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * PluginManager digunakan untuk mendapatkan implementasi InventoryCheckExtension
     * yang di-provide oleh plugin-inventory saat runtime.
     * Nullable karena tidak tersedia saat mode standalone.
     */
    private final PluginManager pluginManager;

    public OrderService(OrderRepository orderRepository,
                        @Autowired(required = false) PluginManager pluginManager) {
        this.orderRepository = orderRepository;
        this.pluginManager = pluginManager;
    }

    public Order createOrder(Long itemId, int quantity) {
        // Cek stok via InventoryCheckExtension dari plugin-inventory
        if (pluginManager != null) {
            List<InventoryCheckExtension> inventoryChecks =
                    pluginManager.getExtensions(InventoryCheckExtension.class);

            if (!inventoryChecks.isEmpty()) {
                InventoryCheckExtension inventoryCheck = inventoryChecks.get(0);

                if (!inventoryCheck.isStockAvailable(itemId, quantity)) {
                    Order order = new Order();
                    order.setInventoryItemId(itemId);
                    order.setQuantity(quantity);
                    order.setStatus("REJECTED"); // stok tidak cukup
                    return orderRepository.save(order);
                }

                // Stok cukup → kurangi stok & buat order
                inventoryCheck.deductStock(itemId, quantity);
            }
        }

        Order order = new Order();
        order.setInventoryItemId(itemId);
        order.setQuantity(quantity);
        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
