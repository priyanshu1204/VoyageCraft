package com.voyagecraft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

@Configuration
public class SchemaFixConfig {
    private final JdbcTemplate jdbcTemplate;

    public SchemaFixConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixSchema() {
        // Fix notifications table - drop stale 'type' column
        safeExecute("ALTER TABLE notifications DROP COLUMN type");

        // Fix expenses table - make description nullable
        safeExecute("ALTER TABLE expenses MODIFY COLUMN description varchar(500) NULL");

        // Fix expenses - drop stale 'payer_id' column not in JPA entity (must drop FK first)
        dropColumnWithFk("expenses", "payer_id");

        // Fix expenses - make notes, paid_by, receipt_url nullable
        safeExecute("ALTER TABLE expenses MODIFY COLUMN notes varchar(500) NULL");
        safeExecute("ALTER TABLE expenses MODIFY COLUMN paid_by varchar(200) NULL");
        safeExecute("ALTER TABLE expenses MODIFY COLUMN receipt_url varchar(100) NULL");

        // Fix weather_forecasts - make nullable columns actually nullable
        safeExecute("ALTER TABLE weather_forecasts MODIFY COLUMN description varchar(500) NULL");

        // US-15: Add checkedIn columns to transport_details and stays
        safeExecute("ALTER TABLE transport_details ADD COLUMN checked_in TINYINT(1) DEFAULT 0");
        safeExecute("ALTER TABLE stays ADD COLUMN checked_in TINYINT(1) DEFAULT 0");

        // US-15: Fix quick_notes photo_urls column to support large Base64 data
        safeExecute("ALTER TABLE quick_notes MODIFY COLUMN photo_urls LONGTEXT");

        // Fix FK constraints on tables referencing trips - ensure ON DELETE CASCADE
        fixForeignKeyCascade("weather_forecasts", "trip_id", "trips");
        fixForeignKeyCascade("offline_caches", "trip_id", "trips");
        fixForeignKeyCascade("sync_logs", "trip_id", "trips");
    }

    /**
     * Drop the existing FK on the given column and re-add it with ON DELETE CASCADE.
     */
    private void fixForeignKeyCascade(String tableName, String columnName, String referencedTable) {
        try {
            // Find the FK constraint name dynamically
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? " +
                "AND REFERENCED_TABLE_NAME = ?",
                tableName, columnName, referencedTable
            );
            for (Map<String, Object> row : rows) {
                String fkName = (String) row.get("CONSTRAINT_NAME");
                safeExecute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + fkName);
            }
            safeExecute("ALTER TABLE " + tableName + " ADD CONSTRAINT fk_" + tableName + "_" + columnName +
                " FOREIGN KEY (" + columnName + ") REFERENCES " + referencedTable + "(id) ON DELETE CASCADE");
        } catch (Exception e) {
            System.out.println("[SchemaFix] SKIP FK fix for " + tableName + "." + columnName + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Drop a stale column, first removing any FK constraints on it.
     */
    private void dropColumnWithFk(String tableName, String columnName) {
        try {
            // Check if column exists
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                tableName, columnName
            );
            if (cols.isEmpty()) {
                System.out.println("[SchemaFix] SKIP: Column " + tableName + "." + columnName + " does not exist");
                return;
            }
            // Find and drop any FK constraints on this column
            List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? " +
                "AND REFERENCED_TABLE_NAME IS NOT NULL",
                tableName, columnName
            );
            for (Map<String, Object> fk : fks) {
                String fkName = (String) fk.get("CONSTRAINT_NAME");
                safeExecute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + fkName);
            }
            // Now drop the column
            safeExecute("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
        } catch (Exception e) {
            System.out.println("[SchemaFix] SKIP drop column " + tableName + "." + columnName + " (" + e.getMessage() + ")");
        }
    }

    private void safeExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
            System.out.println("[SchemaFix] OK: " + sql);
        } catch (Exception e) {
            System.out.println("[SchemaFix] SKIP: " + sql + " (" + e.getMessage() + ")");
        }
    }
}

