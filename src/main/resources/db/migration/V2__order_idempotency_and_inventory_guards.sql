ALTER TABLE customer_orders
    ADD COLUMN idempotency_key VARCHAR(64) NULL,
    ADD COLUMN request_fingerprint CHAR(64) NULL;

UPDATE customer_orders
SET idempotency_key = CONCAT('legacy-', id),
    request_fingerprint = REPEAT('0', 64)
WHERE idempotency_key IS NULL;

ALTER TABLE customer_orders
    MODIFY COLUMN idempotency_key VARCHAR(64) NOT NULL,
    MODIFY COLUMN request_fingerprint CHAR(64) NOT NULL,
    ADD CONSTRAINT uk_customer_orders_idempotency_key UNIQUE (idempotency_key);

ALTER TABLE productos
    ADD CONSTRAINT chk_productos_stock_non_negative CHECK (pro_cantidad >= 0);

CREATE INDEX idx_customer_orders_status_created
    ON customer_orders (status, created_at);

CREATE INDEX idx_order_items_order
    ON order_items (order_id);
