package com.example.api;

import org.pf4j.ExtensionPoint;

/**
 * Extension point yang di-expose oleh plugin-inventory.
 * Plugin lain (misal: plugin-order) bisa menggunakannya untuk
 * mengecek ketersediaan stok sebelum membuat transaksi.
 */
public interface InventoryCheckExtension extends ExtensionPoint {

    /**
     * Cek apakah item tersedia dengan jumlah yang diminta.
     *
     * @param itemId   ID item di inventory
     * @param quantity Jumlah yang dibutuhkan
     * @return true jika stok mencukupi, false jika tidak
     */
    boolean isStockAvailable(Long itemId, int quantity);

    /**
     * Kurangi stok item setelah order dikonfirmasi.
     *
     * @param itemId   ID item di inventory
     * @param quantity Jumlah yang dikurangi
     * @return true jika berhasil, false jika stok tidak cukup
     */
    boolean deductStock(Long itemId, int quantity);

    /**
     * Ambil detail item dalam bentuk DTO shared (bukan entity).
     * Ini cara INTERFACE APPROACH — tidak ada coupling ke class plugin-inventory.
     *
     * @param itemId ID item
     * @return InventoryItemDTO atau null jika tidak ditemukan
     */
    InventoryItemDTO getItemDetails(Long itemId);
}
