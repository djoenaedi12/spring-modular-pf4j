-- ============================================================
-- Plugin Order: V2002 — Seed data awal orders
-- ============================================================

INSERT INTO orders (inventory_item_id, quantity, status) VALUES
    (1, 2, 'CONFIRMED'),   -- 2 Laptop ASUS
    (2, 5, 'CONFIRMED'),   -- 5 Mouse Logitech
    (3, 1, 'REJECTED'),    -- Keyboard — stok tidak cukup (contoh REJECTED)
    (4, 3, 'CONFIRMED'),   -- 3 Monitor LG
    (5, 10, 'REJECTED');   -- 10 Headset — stok tidak cukup (contoh REJECTED)
