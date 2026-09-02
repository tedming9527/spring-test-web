package org.example.springtestweb.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReplicaFlywayIntegrationTest {

  @Autowired
  @Qualifier("replicaFlyway")
  private Flyway replicaFlyway;

  @Autowired
  @Qualifier("replica")
  private DataSource replicaDataSource;

  @Test
  void migratesReplicaGoodsCategorySchema() throws Exception {
    assertEquals("20260902", replicaFlyway.info().current().getVersion().getVersion());

    try (Connection connection = replicaDataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("""
             SELECT COUNT(*)
             FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_name = 'goods_category'
             """);
         ResultSet resultSet = statement.executeQuery()) {
      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getInt(1));
    }
  }
}
