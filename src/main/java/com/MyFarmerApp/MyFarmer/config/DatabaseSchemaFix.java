package com.MyFarmerApp.MyFarmer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaFix implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {

        // Fix 1: Make contract_id nullable in LabourLoanAccount table
        try {
            System.out.println("🔧 Fix 1: Making contract_id nullable in div_labour_loan_account...");
            // Note: lowercase table names — AWS RDS (Linux) is case-sensitive
            jdbcTemplate.execute("ALTER TABLE div_labour_loan_account MODIFY contract_id BIGINT NULL");
            System.out.println("✅ Fix 1 Applied.");
        } catch (Exception e) {
            System.err.println("⚠️ Fix 1 Warning: " + e.getMessage());
        }

        // Fix 2: Fix U_WAGE_TYPE column to support YEARLY value.
        // Root cause: Hibernate @Enumerated(EnumType.STRING) maps to VARCHAR, NOT a MySQL ENUM.
        // However, if the column WAS previously created as a MySQL ENUM (e.g. via a manual script
        // or an older schema), Hibernate's ddl-auto=update will NOT modify it.
        // We ALTER it to VARCHAR(20) which is what Hibernate expects and avoids ENUM restriction issues.
        try {
            System.out.println("🔧 Fix 2: Fixing u_wage_type column to VARCHAR(20) to support YEARLY...");
            jdbcTemplate.execute(
                "ALTER TABLE div_labour MODIFY COLUMN u_wage_type VARCHAR(20) NOT NULL"
            );
            System.out.println("✅ Fix 2 Applied - u_wage_type is now VARCHAR(20), supports DAILY/MONTHLY/YEARLY.");
        } catch (Exception e) {
            // Print root cause for easier debugging
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.err.println("⚠️ Fix 2 Warning: " + cause.getMessage());
        }
    }
}
