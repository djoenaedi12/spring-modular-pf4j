-- ============================================================
-- Plugin Order: V2001 — Create orders table
-- Version range: V2001 – V2999
-- ============================================================

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_item_id BIGINT       NOT NULL,  -- referensi cross-plugin ke inventory_items.id
    quantity         INT           NOT NULL,
    status           VARCHAR(20)   NOT NULL    -- PENDING, CONFIRMED, REJECTED
);
