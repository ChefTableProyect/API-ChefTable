-- El proyecto universitario original creó las claves con INT. Las entidades
-- actuales usan Long/BIGINT, por lo que se normalizan antes de crear pedidos.
-- Cada operación es condicional para que una base nueva también sea válida.

SET @category_fk = (
    SELECT constraint_name
    FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'productos'
      AND column_name = 'cat_id'
      AND referenced_table_name = 'categorias'
    LIMIT 1
);

SET @drop_category_fk_sql = IF(
    @category_fk IS NOT NULL,
    CONCAT('ALTER TABLE productos DROP FOREIGN KEY `', @category_fk, '`'),
    'SELECT 1'
);
PREPARE drop_category_fk_statement FROM @drop_category_fk_sql;
EXECUTE drop_category_fk_statement;
DEALLOCATE PREPARE drop_category_fk_statement;

SET @categories_exist = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'categorias'
);
SET @alter_categories_sql = IF(
    @categories_exist = 1,
    'ALTER TABLE categorias MODIFY COLUMN cat_id BIGINT NOT NULL AUTO_INCREMENT',
    'SELECT 1'
);
PREPARE alter_categories_statement FROM @alter_categories_sql;
EXECUTE alter_categories_statement;
DEALLOCATE PREPARE alter_categories_statement;

SET @products_exist = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'productos'
);
SET @alter_products_sql = IF(
    @products_exist = 1,
    'ALTER TABLE productos MODIFY COLUMN pro_id BIGINT NOT NULL AUTO_INCREMENT, MODIFY COLUMN cat_id BIGINT NULL',
    'SELECT 1'
);
PREPARE alter_products_statement FROM @alter_products_sql;
EXECUTE alter_products_statement;
DEALLOCATE PREPARE alter_products_statement;

SET @add_category_fk_sql = IF(
    @products_exist = 1 AND @categories_exist = 1,
    'ALTER TABLE productos ADD CONSTRAINT fk_productos_categoria FOREIGN KEY (cat_id) REFERENCES categorias (cat_id) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE add_category_fk_statement FROM @add_category_fk_sql;
EXECUTE add_category_fk_statement;
DEALLOCATE PREPARE add_category_fk_statement;
