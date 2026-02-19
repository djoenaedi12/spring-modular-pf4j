-- ============================================================
-- Plugin Inventory: V1001 — Create inventory_items table
-- Range versi plugin ini: V1001 – V1999
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_items (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    quantity INT,
    price    DOUBLE
);
