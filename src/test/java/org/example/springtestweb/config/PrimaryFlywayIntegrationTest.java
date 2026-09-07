package org.example.springtestweb.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PrimaryFlywayIntegrationTest {

  @Autowired
  @Qualifier("primaryFlyway")
  private Flyway primaryFlyway;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void migratesPrimarySchemaThroughCategoryVersionMigration() {
    assertEquals("20260831.1", primaryFlyway.info().current().getVersion().getVersion());
    Integer columnCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'goods_category'
          AND column_name = 'category_version'
        """, Integer.class);
    assertNotNull(columnCount);
    assertEquals(1, columnCount);
  }
}
