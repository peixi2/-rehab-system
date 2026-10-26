package com.rehab.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@Order(1)
public class DemoDataSeeder implements CommandLineRunner {

    private static final String SEED_VERSION = "demo-v1";
    private static final List<String> BUSINESS_TABLES = List.of(
            "sys_user",
            "doctor",
            "bed",
            "assessment_item",
            "treatment_item",
            "patient",
            "assessment_task",
            "assessment_record",
            "treatment_record",
            "scale_item",
            "scale_option",
            "audit_log"
    );

    private final JdbcTemplate jdbcTemplate;
    private final boolean seedEnabled;

    public DemoDataSeeder(JdbcTemplate jdbcTemplate,
                          @Value("${app.demo.seed-enabled:true}") boolean seedEnabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("演示数据初始化已关闭");
            return;
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_seed_version (
                    seed_key VARCHAR(64) PRIMARY KEY,
                    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_seed_version WHERE seed_key = ?",
                Integer.class,
                SEED_VERSION);
        if (applied != null && applied > 0) {
            log.info("演示数据 {} 已存在，跳过初始化", SEED_VERSION);
            return;
        }

        String populatedTable = findPopulatedBusinessTable();
        if (populatedTable != null) {
            log.warn("数据库已有业务数据（表 {}），为避免覆盖数据，跳过演示数据初始化", populatedTable);
            return;
        }

        executeSeedScripts();

        jdbcTemplate.update(
                "INSERT INTO app_seed_version(seed_key) VALUES (?)",
                SEED_VERSION);
        log.info("演示数据 {} 初始化完成", SEED_VERSION);
    }

    String findPopulatedBusinessTable() {
        for (String table : BUSINESS_TABLES) {
            Integer hasRows = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + table + "`",
                    Integer.class);
            if (hasRows != null && hasRows > 0) {
                return table;
            }
        }
        return null;
    }

    void executeSeedScripts() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        populator.setContinueOnError(false);
        populator.addScript(new ClassPathResource("db/seed.sql"));
        populator.addScript(new ClassPathResource("db/scale_seed.sql"));
        populator.execute(jdbcTemplate.getDataSource());
    }
}
