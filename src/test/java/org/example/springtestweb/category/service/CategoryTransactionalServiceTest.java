package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.redis.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoryTransactionalServiceTest {
  private static final  long CATEGORY_ID = 1101L;
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private CategoryTransactionalService categoryTransactionalService;
  @Autowired
  private RedisService redisService;

  @Test
  void updateName_rollback_shouldKeepDatabaseAndCache() {
    String rollbackName = "TX_ROLLBACK_TEST";

    Category category = categoryMapper.selectById(CATEGORY_ID);
    assertNotNull(category, "分类不存在，id=" + CATEGORY_ID);

    String cacheKey = "category:children:" + category.getParentId();
    redisService.setObject(cacheKey, category, Duration.ofMinutes(10));

    String oldName = category.getName();
    category.setName(rollbackName);

    try {
      RuntimeException exception = assertThrows(RuntimeException.class, () ->
        categoryTransactionalService.updateName(category, cacheKey, rollbackName)
      );
      assertEquals("报错开关",  exception.getMessage());

      Category dbCategory = categoryMapper.selectById(category.getId());
      assertNotNull(dbCategory, "回滚后分类不应消失");
      assertEquals(oldName, dbCategory.getName());
      Category cacheCategory = redisService.getObject(cacheKey,  Category.class);
      assertNotNull(cacheCategory, "事务回滚后原缓存应保留");
      assertEquals(oldName, cacheCategory.getName());
    } finally {
      Category dbCategory = categoryMapper.selectById(CATEGORY_ID);
      if (dbCategory != null) {
        dbCategory.setName(oldName);
        categoryMapper.updateById(dbCategory);
      }
      redisService.delete(cacheKey);
    }
  }
  @Test
  void updateName_commit_shouldUpdateDatabaseAndDeleteCache() {
    Category category = categoryMapper.selectById(CATEGORY_ID);
    assertNotNull(category, "分类不存在，id=" + CATEGORY_ID);
    String cacheKey = "category:children:" + category.getParentId();
    redisService.setObject(cacheKey, category, Duration.ofMinutes(10));

    String oldName = category.getName();

    String newName = "TX_SUBMIT_TEST";
    category.setName(newName);

    try {
      Boolean updated = categoryTransactionalService.updateName(category, cacheKey, newName);
      assertTrue(updated, "事务提交路径的分类更新应成功");
      Category dbCategory = categoryMapper.selectById(CATEGORY_ID);
      assertNotNull(dbCategory, "事务提交后分类记录不应该消失");
      assertEquals(newName, dbCategory.getName(), "事务提交后数据库应保存新名称");
      Category cacheCategory = redisService.getObject(cacheKey,  Category.class);
      assertNull(cacheCategory, "事务提交后分类缓存应被删除");
    } finally {
      Category dbCategory = categoryMapper.selectById(CATEGORY_ID);
      if (dbCategory != null) {
        dbCategory.setName(oldName);
        categoryMapper.updateById(dbCategory);
      }
      redisService.delete(cacheKey);
    }
  }
}
