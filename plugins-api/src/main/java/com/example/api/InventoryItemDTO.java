package com.example.api;

/**
 * Shared DTO untuk data item inventory antar plugin.
 * Didefinisikan di plugins-api agar tidak ada coupling langsung ke entity plugin-inventory.
 */
public record InventoryItemDTO(
        Long id,
        String name,
        Integer quantity,
        Double price
) {}
