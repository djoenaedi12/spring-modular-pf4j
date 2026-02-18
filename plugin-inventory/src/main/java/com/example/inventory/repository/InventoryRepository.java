package com.example.inventory.repository;

import com.example.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    // Anda bisa menambah custom query di sini jika perlu
}
