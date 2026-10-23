package com.rehab.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SeedScriptSafetyTest {

    @Test
    void seedScriptsDoNotContainDestructiveTableStatements() throws IOException {
        assertNonDestructive("db/seed.sql");
        assertNonDestructive("db/scale_seed.sql");
    }

    private void assertNonDestructive(String path) throws IOException {
        String sql = new ClassPathResource(path)
                .getContentAsString(StandardCharsets.UTF_8)
                .toUpperCase(Locale.ROOT);
        assertFalse(sql.contains("TRUNCATE TABLE"), path);
        assertFalse(sql.contains("DELETE FROM"), path);
        assertFalse(sql.contains("DROP TABLE"), path);
    }
}
