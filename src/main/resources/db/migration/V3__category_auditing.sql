-- La entidad Categoria ya declaraba auditoría, pero el esquema universitario
-- original no incluía esas columnas. Cada cambio es condicional para soportar
-- tanto bases nuevas como bases locales existentes.

SET @created_at_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'categorias' AND column_name = 'created_at'
);
SET @created_at_sql = IF(
    @created_at_exists = 0,
    'ALTER TABLE categorias ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
    'SELECT 1'
);
PREPARE created_at_statement FROM @created_at_sql;
EXECUTE created_at_statement;
DEALLOCATE PREPARE created_at_statement;

SET @updated_at_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'categorias' AND column_name = 'updated_at'
);
SET @updated_at_sql = IF(
    @updated_at_exists = 0,
    'ALTER TABLE categorias ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
    'SELECT 1'
);
PREPARE updated_at_statement FROM @updated_at_sql;
EXECUTE updated_at_statement;
DEALLOCATE PREPARE updated_at_statement;

SET @created_by_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'categorias' AND column_name = 'created_by'
);
SET @created_by_sql = IF(
    @created_by_exists = 0,
    'ALTER TABLE categorias ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT ''system''',
    'SELECT 1'
);
PREPARE created_by_statement FROM @created_by_sql;
EXECUTE created_by_statement;
DEALLOCATE PREPARE created_by_statement;

SET @updated_by_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'categorias' AND column_name = 'updated_by'
);
SET @updated_by_sql = IF(
    @updated_by_exists = 0,
    'ALTER TABLE categorias ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT ''system''',
    'SELECT 1'
);
PREPARE updated_by_statement FROM @updated_by_sql;
EXECUTE updated_by_statement;
DEALLOCATE PREPARE updated_by_statement;
