package com.example.inventory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    // Mendapatkan semua data barang
    @GetMapping
    public List<InventoryItem> getAllItems() {
        System.out.println("Udh kepanggil");
        return this.inventoryRepository.findAll();
    }

    // Menambah barang baru
    @PostMapping
    public InventoryItem createItem(@RequestBody InventoryItem item) {
        return this.inventoryRepository.save(item);
    }

    // Mendapatkan detail barang berdasarkan ID
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        return this.inventoryRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Menghapus barang
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        this.inventoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
