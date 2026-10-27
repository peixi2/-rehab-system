package com.rehab.system.config;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public final class DatabaseFallbackConfigurer {

    private static final int LOGIN_TIMEOUT_SECONDS = 15;
    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int SOCKET_TIMEOUT_MILLIS = 15_000;

    private DatabaseFallbackConfigurer() {
    }

    public static void configure() {
        String externalUrl = env("DB_URL");
        boolean runningOnRender = "true".equalsIgnoreCase(env("RENDER"));
        boolean fallbackEnabled = booleanEnv("DB_FALLBACK_EMBEDDED", runningOnRender);
        if (!fallbackEnabled) {
            return;
        }

        if (externalUrl.isBlank()) {
            configureEmbeddedDatabase(new SQLException("DB_URL is not configured"));
            return;
        }

        try {
            runExternalDatabasePreflight(
                    externalUrl,
                    env("DB_USERNAME"),
                    env("DB_PASSWORD"));
            configureExternalDatabaseTimeouts();
            log.info("External MySQL preflight succeeded");
        } catch (SQLException | TimeoutException error) {
            configureEmbeddedDatabase(error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            configureEmbeddedDatabase(error);
        }
    }

    private static void runExternalDatabasePreflight(
            String url,
            String username,
            String password) throws SQLException, TimeoutException, InterruptedException {
        FutureTask<Void> preflight = new FutureTask<>(() -> {
            DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("connectTimeout", Integer.toString(CONNECT_TIMEOUT_MILLIS));
            properties.setProperty("socketTimeout", Integer.toString(SOCKET_TIMEOUT_MILLIS));
            properties.setProperty("tcpKeepAlive", "true");
            try (Connection ignored = DriverManager.getConnection(url, properties)) {
                return null;
            }
        });
        Thread thread = new Thread(preflight, "external-database-preflight");
        thread.setDaemon(true);
        thread.start();

        try {
            preflight.get(LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof SQLException sqlError) {
                throw sqlError;
            }
            throw new SQLException("External database preflight failed", cause);
        } catch (TimeoutException error) {
            preflight.cancel(true);
            throw error;
        }
    }

    private static void configureExternalDatabaseTimeouts() {
        System.setProperty(
                "spring.datasource.hikari.data-source-properties.connectTimeout",
                Integer.toString(CONNECT_TIMEOUT_MILLIS));
        System.setProperty(
                "spring.datasource.hikari.data-source-properties.socketTimeout",
                Integer.toString(SOCKET_TIMEOUT_MILLIS));
        System.setProperty("spring.datasource.hikari.data-source-properties.tcpKeepAlive", "true");
        System.setProperty("spring.datasource.hikari.connection-timeout", "15000");
        System.setProperty("spring.datasource.hikari.validation-timeout", "5000");
    }

    static void configureEmbeddedDatabase(Throwable error) {
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
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
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
