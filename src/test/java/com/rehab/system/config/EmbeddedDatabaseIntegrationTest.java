package com.rehab.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:rehab-fallback;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.hikari.connection-init-sql=SELECT 1",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "app.demo.password=test-demo-password"
        })
class EmbeddedDatabaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAUsableSeededDemoDatabase() {
        assertEquals(8, count("patient"));
        assertEquals(5, count("doctor"));
        assertEquals(8, count("assessment_item"));
        assertEquals(73, count("scale_item"));
        assertEquals(5, count("sys_user"));
    }

    private int count(String table) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `" + table + "`",
                Integer.class);
        return value == null ? 0 : value;
    }
}
