package com.rehab.system.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseFallbackConfigurerTest {

    private static final List<String> SYSTEM_PROPERTIES = List.of(
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.datasource.driver-class-name",
            "spring.datasource.hikari.connection-init-sql",
            "spring.jpa.database-platform",
            "spring.jpa.hibernate.ddl-auto"
    );

    private final Map<String, String> originalValues = new HashMap<>();

    @BeforeEach
    void rememberSystemProperties() {
        SYSTEM_PROPERTIES.forEach(key -> originalValues.put(key, System.getProperty(key)));
    }

    @AfterEach
    void restoreSystemProperties() {
        originalValues.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    @Test
    void fallbackOverridesProductionDatabaseSettings() {
        DatabaseFallbackConfigurer.configureEmbeddedDatabase(
                new SQLException("database unavailable"));

        assertTrue(System.getProperty("spring.datasource.url").startsWith("jdbc:h2:file:"));
        assertEquals("org.h2.Driver", System.getProperty("spring.datasource.driver-class-name"));
        assertEquals("org.hibernate.dialect.H2Dialect", System.getProperty("spring.jpa.database-platform"));
        assertEquals("update", System.getProperty("spring.jpa.hibernate.ddl-auto"));
    }
}
