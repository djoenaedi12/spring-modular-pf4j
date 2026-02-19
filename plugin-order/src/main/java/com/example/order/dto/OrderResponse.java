package com.example.order.dto;

import com.example.api.InventoryItemDTO;

/**
 * Response DTO untuk order yang sudah dienrich dengan detail item.
 *
 * Menggunakan InventoryItemDTO (dari plugins-api) bukan InventoryItem (dari plugin-inventory).
 * Ini adalah INTERFACE APPROACH — plugin-order tidak tahu class dari plugin-inventory.
 */
public record OrderResponse(
        Long orderId,
        String status,
        Integer quantity,
        InventoryItemDTO item   // ← DTO dari plugins-api, bukan entity dari plugin-inventory
) {}
