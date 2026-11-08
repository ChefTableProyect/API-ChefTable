CREATE TABLE IF NOT EXISTS categorias (
    cat_id BIGINT NOT NULL AUTO_INCREMENT,
    cat_nombre VARCHAR(120) NOT NULL,
    cat_descripcion VARCHAR(300),
    PRIMARY KEY (cat_id)
);

CREATE TABLE IF NOT EXISTS usuarios (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    address VARCHAR(180) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    PRIMARY KEY (user_id),
    CONSTRAINT uk_usuarios_address UNIQUE (address)
);

CREATE TABLE IF NOT EXISTS productos (
    pro_id BIGINT NOT NULL AUTO_INCREMENT,
    pro_nombre VARCHAR(120) NOT NULL,
    pro_cantidad INT NOT NULL DEFAULT 0,
    pro_precio DECIMAL(12, 2) NOT NULL,
    pro_active BOOLEAN NOT NULL DEFAULT TRUE,
    cat_id BIGINT,
    PRIMARY KEY (pro_id),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (cat_id) REFERENCES categorias (cat_id)
);

SET @role_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'role'
);
SET @role_sql = IF(
    @role_exists = 0,
    'ALTER TABLE usuarios ADD COLUMN role VARCHAR(30) NOT NULL DEFAULT ''ADMIN''',
    'SELECT 1'
);
PREPARE role_statement FROM @role_sql;
EXECUTE role_statement;
DEALLOCATE PREPARE role_statement;

ALTER TABLE productos
    MODIFY COLUMN pro_precio DECIMAL(12, 2) NOT NULL;

SET @active_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'pro_active'
);
SET @active_sql = IF(
    @active_exists = 0,
    'ALTER TABLE productos ADD COLUMN pro_active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE active_statement FROM @active_sql;
EXECUTE active_statement;
DEALLOCATE PREPARE active_statement;

CREATE TABLE restaurant_tables (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_restaurant_tables_code UNIQUE (code)
);

CREATE TABLE customer_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_code VARCHAR(20) NOT NULL,
    table_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_customer_orders_public_code UNIQUE (public_code),
    CONSTRAINT fk_customer_orders_table FOREIGN KEY (table_id) REFERENCES restaurant_tables (id)
);

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    notes VARCHAR(300),
    subtotal DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES customer_orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES productos (pro_id)
);

INSERT IGNORE INTO restaurant_tables (code, name, active) VALUES
    ('MESA-01', 'Mesa 1', TRUE),
    ('MESA-02', 'Mesa 2', TRUE),
    ('MESA-03', 'Mesa 3', TRUE);
