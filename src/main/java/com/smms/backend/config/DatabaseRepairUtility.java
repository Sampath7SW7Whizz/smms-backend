package com.smms.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility to repair database schema issues that Hibernate's ddl-auto=update cannot handle.
 * Specifically, it removes stale global unique constraints on item_code that prevent multiple users
 * from having the same default products.
 */
@Component
@Order(1) // Run early during startup
public class DatabaseRepairUtility implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseRepairUtility.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        repairProductConstraints();
        repairProductsNullableFields();
        removeLegacyProductColumns();
        repairSalesSchema();
    }

    private void repairProductConstraints() {
        try {
            logger.info("Checking for stale unique constraints on products(item_code)...");

            // H2 2.x exposes constraint metadata through TABLE_CONSTRAINTS and
            // CONSTRAINT_COLUMN_USAGE.
            String query = """
                    SELECT tc.CONSTRAINT_NAME
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                    JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                      ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME
                    WHERE tc.TABLE_NAME = 'PRODUCTS'
                      AND tc.CONSTRAINT_TYPE = 'UNIQUE'
                    GROUP BY tc.CONSTRAINT_NAME
                    HAVING COUNT(*) = 1 AND MAX(ccu.COLUMN_NAME) = 'ITEM_CODE'
                    """;

            List<Map<String, Object>> constraints = jdbcTemplate.queryForList(query);

            if (constraints.isEmpty()) {
                logger.info("No stale global unique constraints found on products.item_code.");
                return;
            }

            for (Map<String, Object> constraint : constraints) {
                String constraintName = (String) constraint.get("CONSTRAINT_NAME");
                logger.warn("Found stale unique constraint: {}. DROPPING IT to allow multi-user isolation.", constraintName);
                
                try {
                    jdbcTemplate.execute("ALTER TABLE PRODUCTS DROP CONSTRAINT " + constraintName);
                    logger.info("Successfully dropped constraint: {}", constraintName);
                } catch (Exception e) {
                    logger.error("Failed to drop constraint {}: {}", constraintName, e.getMessage());
                }
            }
            
            logger.info("Database schema repair for products table completed.");

        } catch (Exception e) {
            logger.error("Error during database schema repair: {}", e.getMessage());
            // We don't throw here to avoid preventing app startup if the repair logic itself has an issue
        }
    }

    private void repairProductsNullableFields() {
        try {
            logger.info("Ensuring optional product fields accept NULL values...");

            List<String> productColumns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PRODUCTS'",
                    String.class);

            if (productColumns.isEmpty()) {
                logger.info("Products table does not exist yet. Skipping nullable field repair.");
                return;
            }

            relaxNotNullOnColumn("PRODUCTS", "ITEM_NAME");
            relaxNotNullOnColumn("PRODUCTS", "ITEM_CODE");
            relaxNotNullOnColumn("PRODUCTS", "CATEGORY");
            relaxNotNullOnColumn("PRODUCTS", "QUANTITY");
            relaxNotNullOnColumn("PRODUCTS", "EXPIRY_DATE");

            logger.info("Product nullable field repair completed.");
        } catch (Exception e) {
            logger.warn("Could not complete product nullable field repair: {}", e.getMessage());
        }
    }

    private void relaxNotNullOnColumn(String tableName, String columnName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " SET NULL");
        } catch (Exception e) {
            logger.warn("Could not relax NOT NULL on {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }

    private void removeLegacyProductColumns() {
        try {
            logger.info("Removing deprecated product columns (TAX_RATE, SUPPLIER_NAME) if present...");

            List<String> productColumns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PRODUCTS'",
                    String.class);

            if (productColumns.contains("TAX_RATE")) {
                jdbcTemplate.execute("ALTER TABLE PRODUCTS DROP COLUMN TAX_RATE");
                logger.info("Dropped PRODUCTS.TAX_RATE");
            }

            if (productColumns.contains("SUPPLIER_NAME")) {
                jdbcTemplate.execute("ALTER TABLE PRODUCTS DROP COLUMN SUPPLIER_NAME");
                logger.info("Dropped PRODUCTS.SUPPLIER_NAME");
            }
        } catch (Exception e) {
            logger.warn("Could not remove deprecated product columns: {}", e.getMessage());
        }
    }

    private void repairSalesSchema() {
        try {
            logger.info("Checking sales table compatibility for invoice_no migration...");

            List<String> saleColumns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'SALES'",
                    String.class);

            if (saleColumns.isEmpty()) {
                logger.info("Sales table does not exist yet. Skipping sales schema repair.");
                return;
            }

            boolean hasInvoiceNo = saleColumns.contains("INVOICE_NO");
            boolean hasLegacyTransactionId = saleColumns.contains("TRANSACTION_ID");

            if (!hasInvoiceNo && hasLegacyTransactionId) {
                logger.warn("Missing INVOICE_NO column detected. Creating it from legacy TRANSACTION_ID values.");
                jdbcTemplate.execute("ALTER TABLE SALES ADD COLUMN INVOICE_NO VARCHAR(255)");
                hasInvoiceNo = true;
            }

            if (hasInvoiceNo && hasLegacyTransactionId) {
                logger.warn("Legacy TRANSACTION_ID column detected. Backfilling INVOICE_NO and relaxing legacy column.");
                jdbcTemplate.execute("""
                        UPDATE SALES
                        SET INVOICE_NO = COALESCE(INVOICE_NO, TRANSACTION_ID)
                        WHERE INVOICE_NO IS NULL OR TRIM(INVOICE_NO) = ''
                        """);

                // Older databases can still have TRANSACTION_ID marked NOT NULL, which
                // breaks inserts because the current entity only writes invoice_no.
                jdbcTemplate.execute("ALTER TABLE SALES ALTER COLUMN TRANSACTION_ID SET NULL");
            }

            if (hasInvoiceNo) {
                jdbcTemplate.execute("""
                        UPDATE SALES
                        SET INVOICE_NO = CONCAT('INV', RIGHT(CONCAT('000000', ID), 6))
                        WHERE INVOICE_NO IS NULL OR TRIM(INVOICE_NO) = ''
                        """);
            }

            repairLegacySalesConstraints();
            logger.info("Sales schema repair completed.");
        } catch (Exception e) {
            logger.error("Error during sales schema repair: {}", e.getMessage());
        }
    }

    private void repairLegacySalesConstraints() {
        try {
            List<Map<String, Object>> constraints = jdbcTemplate.queryForList("""
                    SELECT tc.CONSTRAINT_NAME, GROUP_CONCAT(ccu.COLUMN_NAME ORDER BY ccu.COLUMN_NAME) AS COLUMN_NAMES
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                    JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE ccu
                      ON tc.CONSTRAINT_NAME = ccu.CONSTRAINT_NAME
                    WHERE tc.TABLE_NAME = 'SALES'
                      AND tc.CONSTRAINT_TYPE = 'UNIQUE'
                    GROUP BY tc.CONSTRAINT_NAME
                    """);

            for (Map<String, Object> constraint : constraints) {
                String constraintName = (String) constraint.get("CONSTRAINT_NAME");
                String columns = String.valueOf(constraint.get("COLUMN_NAMES"));
                List<String> normalizedColumns = List.of(columns.split(","))
                        .stream()
                        .map(String::trim)
                        .sorted()
                        .collect(Collectors.toList());

                if (normalizedColumns.equals(List.of("TRANSACTION_ID"))
                        || normalizedColumns.equals(List.of("TRANSACTION_ID", "USER_ID"))) {
                    logger.warn("Dropping stale sales unique constraint {} on legacy columns {}", constraintName, columns);
                    jdbcTemplate.execute("ALTER TABLE SALES DROP CONSTRAINT " + constraintName);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not inspect legacy sales constraints: {}", e.getMessage());
        }
    }
}
