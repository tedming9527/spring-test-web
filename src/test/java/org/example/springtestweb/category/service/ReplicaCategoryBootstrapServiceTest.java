package org.example.springtestweb.category.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReplicaCategoryBootstrapServiceTest {

  @Autowired
  private ReplicaCategoryBootstrapService replicaCategoryBootstrapService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  @Qualifier("replica")
  private JdbcTemplate replicaJdbcTemplate;

  @Test
  void copiesAllPrimaryCategoriesAndCanBeRunAgain() {
    replicaCategoryBootstrapService.initializeGoodsCategories();
    replicaCategoryBootstrapService.initializeGoodsCategories();

    Integer primaryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM goods_category", Integer.class);
    Integer replicaCount = replicaJdbcTemplate.queryForObject("SELECT COUNT(*) FROM goods_category", Integer.class);
    Integer mismatchCount = replicaJdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM springtestweb.goods_category primary_category
        LEFT JOIN goods_category replica_category ON replica_category.id = primary_category.id
        WHERE replica_category.id IS NULL
           OR replica_category.name <> primary_category.name
           OR replica_category.category_version <> primary_category.category_version
        """, Integer.class);

    assertEquals(primaryCount, replicaCount);
    assertEquals(0, mismatchCount);
  }
}
