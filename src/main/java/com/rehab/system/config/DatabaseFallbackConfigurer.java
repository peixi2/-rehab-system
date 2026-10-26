package com.rehab.system.config;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public final class DatabaseFallbackConfigurer {

    private static final int LOGIN_TIMEOUT_SECONDS = 4;

    private DatabaseFallbackConfigurer() {
    }

    public static void configure() {
        String externalUrl = env("DB_URL");
        boolean runningOnRender = "true".equalsIgnoreCase(env("RENDER"));
        boolean fallbackEnabled = booleanEnv("DB_FALLBACK_EMBEDDED", runningOnRender);
        if (!fallbackEnabled || externalUrl.isBlank()) {
            return;
        }

        try {
            DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
            try (Connection ignored = DriverManager.getConnection(
                    externalUrl,
                    env("DB_USERNAME"),
                    env("DB_PASSWORD"))) {
                log.info("External MySQL preflight succeeded");
            }
        } catch (SQLException error) {
            configureEmbeddedDatabase(error);
        }
    }

    private static void configureEmbeddedDatabase(SQLException error) {
        String configuredPath = env("DB_FALLBACK_PATH");
        Path databasePath = configuredPath.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "rehab-system-demo")
                : Path.of(configuredPath);
        String url = "jdbc:h2:file:" + databasePath.toAbsolutePath().normalize()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
                + ";AUTO_SERVER=FALSE";

        System.setProperty("spring.datasource.url", url);
        System.setProperty("spring.datasource.username", "sa");
        System.setProperty("spring.datasource.password", "");
        System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
        System.setProperty("spring.datasource.hikari.connection-init-sql", "SELECT 1");
        System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        log.warn(
                "External MySQL is unavailable; using the embedded demo database ({})",
                error.getClass().getSimpleName());
    }

    private static boolean booleanEnv(String name, boolean defaultValue) {
        String raw = env(name);
        if (raw.isBlank()) {
            return defaultValue;
        }
        return switch (raw.toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> throw new IllegalArgumentException(name + " must be a boolean value");
        };
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }
}
