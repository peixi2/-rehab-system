package com.rehab.system.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void doesNotSeedWhenAnyBusinessTableContainsData() {
        DemoDataSeeder seeder = spy(new DemoDataSeeder(jdbcTemplate, true));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("demo-v1")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(*)"), eq(Integer.class)))
                .thenAnswer(invocation -> invocation.<String>getArgument(0).contains("`patient`") ? 1 : 0);

        seeder.run();

        verify(seeder, never()).executeSeedScripts();
        verify(jdbcTemplate, never()).update(anyString(), eq("demo-v1"));
    }

    @Test
    void seedsAndRecordsVersionWhenEveryBusinessTableIsEmpty() {
        DemoDataSeeder seeder = spy(new DemoDataSeeder(jdbcTemplate, true));
        doNothing().when(seeder).executeSeedScripts();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("demo-v1")))
                .thenReturn(0);
        when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(*)"), eq(Integer.class)))
                .thenReturn(0);

        seeder.run();

        verify(seeder).executeSeedScripts();
        verify(jdbcTemplate).update(
                "INSERT INTO app_seed_version(seed_key) VALUES (?)",
                "demo-v1");
    }

    @Test
    void skipsAllChecksWhenSeedVersionAlreadyExists() {
        DemoDataSeeder seeder = spy(new DemoDataSeeder(jdbcTemplate, true));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("demo-v1")))
                .thenReturn(1);

        seeder.run();

        verify(jdbcTemplate, never()).queryForObject(startsWith("SELECT COUNT(*)"), eq(Integer.class));
        verify(seeder, never()).executeSeedScripts();
    }
}
