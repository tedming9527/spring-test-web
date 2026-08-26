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
  void updateName_commit_shouldUpdateDatabaseAndDeleteCache() {
    Category category = categoryMapper.selectById(CATEGORY_ID);
    String cacheKey = "category:children" + CATEGORY_ID;
    String lockKey = "lock:" + cacheKey;

    assertNotNull(category, "测试分类不存在，id=" + CATEGORY_ID);

    String oldName = category.getName();
    String newName = "TX_AUTO_COMMIT_TEST";

    redisService.set(cacheKey, "old-cache", Duration.ofMinutes(1));
    category.setName(newName);

    try {
      Boolean updated = categoryTransactionalService.updateName(category, cacheKey, newName);
      assertTrue(updated);
      Category updatedCategory = categoryMapper.selectById(category.getId());
      assertEquals(newName, updatedCategory.getName());
      assertNull(redisService.get(cacheKey));
    } finally {
      Category updatedCategory = categoryMapper.selectById(category.getId());
      updatedCategory.setName(oldName);
      categoryMapper.updateById(updatedCategory);
      redisService.delete(lockKey);
    }
  }
}
