package com.example.inventory;

import com.example.api.InventoryCheckExtension;
import com.example.api.InventoryItemDTO;
import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Implementasi InventoryCheckExtension dari plugin-inventory.
 * Digunakan oleh plugin lain (misal: plugin-order) untuk cek & kurangi stok.
 */
@Extension
public class InventoryCheckExtensionImpl implements InventoryCheckExtension {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public boolean isStockAvailable(Long itemId, int quantity) {
        Optional<InventoryItem> item = inventoryRepository.findById(itemId);
        return item.isPresent() && item.get().getQuantity() >= quantity;
    }

    @Override
    public boolean deductStock(Long itemId, int quantity) {
        Optional<InventoryItem> optItem = inventoryRepository.findById(itemId);
        if (optItem.isEmpty())
            return false;

        InventoryItem item = optItem.get();
        if (item.getQuantity() < quantity)
            return false;

        item.setQuantity(item.getQuantity() - quantity);
        inventoryRepository.save(item);
        return true;
    }

    /**
     * INTERFACE APPROACH: konversi entity ke shared DTO.
     * plugin-order tidak tahu tentang entity InventoryItem — hanya tahu
     * InventoryItemDTO.
     */
    @Override
    public InventoryItemDTO getItemDetails(Long itemId) {
        return inventoryRepository.findById(itemId)
                .map(item -> new InventoryItemDTO(
                        item.getId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice()))
                .orElse(null);
    }
}
