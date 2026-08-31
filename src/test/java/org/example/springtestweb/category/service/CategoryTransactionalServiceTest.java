package org.example.springtestweb.category.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.redis.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

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
  @Autowired
  private CategoryChangeEventMapper categoryChangeEventMapper;

  @Test
  @Transactional
  void updateName_rollback_shouldKeepDatabaseAndCache() {
    String rollbackName = "TX_AUTO_ROLLBACK_" + UUID.randomUUID();

    Category category = categoryMapper.selectById(CATEGORY_ID);
    assertNotNull(category, "分类不存在，id=" + CATEGORY_ID);

    String cacheKey = "category:children:" + category.getParentId();
    redisService.setObject(cacheKey, category, Duration.ofMinutes(10));

    String oldName = category.getName();
    Long oldVersion = category.getCategoryVersion();
    category.setName(rollbackName);

    try {
      Boolean updated = categoryTransactionalService.updateName(category, cacheKey);
      assertTrue(updated, "回滚前数据库update应执行成功");

      TestTransaction.flagForRollback();
      TestTransaction.end();

      Category dbCategory = categoryMapper.selectById(category.getId());
      assertNotNull(dbCategory, "回滚后分类不应消失");
      assertEquals(oldName, dbCategory.getName(), "回滚后分类名称不应变化");
      assertEquals(oldVersion, dbCategory.getCategoryVersion(), "回滚后分类版本不应该变化");

      Category cacheCategory = redisService.getObject(cacheKey,  Category.class);
      assertNotNull(cacheCategory, "事务回滚后原缓存应保留");
      assertEquals(oldName, cacheCategory.getName());

      List<CategoryChangeEvent> events = findNameChangedEvents(
        oldVersion + 1, rollbackName
      );
      assertTrue(events.isEmpty(), "事务回滚后不应留下分类变更事件");

    } finally {
      if (TestTransaction.isActive()) {
        TestTransaction.flagForRollback();
        TestTransaction.end();
      }

      restoreCategory(oldName, oldVersion);
      deleteNameChangedEvents(oldVersion + 1, rollbackName);
      redisService.delete(cacheKey);
    }
  }

  @Test
  void updateName_commit_shouldUpdateDatabaseAndDeleteCache() {
    String newName = "TX_AUTO_COMMIT_" + UUID.randomUUID();
    Long createdEventId = null;

    Category category = categoryMapper.selectById(CATEGORY_ID);
    assertNotNull(category, "分类不存在，id=" + CATEGORY_ID);

    String cacheKey = "category:children:" + category.getParentId();
    redisService.setObject(cacheKey, category, Duration.ofMinutes(10));

    String oldName = category.getName();
    Long oldVersion = category.getCategoryVersion();
    category.setName(newName);

    try {
      Boolean updated = categoryTransactionalService.updateName(category, cacheKey);
      assertTrue(updated, "事务提交路径的分类更新应成功");

      Category dbCategory = categoryMapper.selectById(CATEGORY_ID);
      assertNotNull(dbCategory, "事务提交后分类记录不应该消失");
      assertEquals(newName, dbCategory.getName(), "事务提交后数据库应保存新名称");
      assertEquals(oldVersion + 1, dbCategory.getCategoryVersion(), "事务提交后分类版本应加一");
      assertEquals(oldVersion + 1, category.getCategoryVersion(), "内存对象应同步为新版本");

      Category cacheCategory = redisService.getObject(cacheKey,  Category.class);
      assertNull(cacheCategory, "事务提交后分类缓存应被删除");


      List<CategoryChangeEvent> events = findNameChangedEvents(
        oldVersion + 1, newName
      );
      assertEquals(1, events.size(), "事务提交后应写入一条分类变更事件");

      CategoryChangeEvent event = events.get(0);
      createdEventId = event.getId();

      assertEquals(CATEGORY_ID, event.getCategoryId());
      assertEquals(oldVersion + 1, event.getCategoryVersion());
      assertEquals("CATEGORY_NAME_CHANGED", event.getEventType());
      assertEquals("{\"name\":\"" + newName + "\"}", event.getPayload());
      assertEquals("PENDING", event.getStatus());
      assertEquals(0, event.getRetryCount());

    } finally {
      if (createdEventId != null) {
        categoryChangeEventMapper.deleteById(createdEventId);
      } else {
        deleteNameChangedEvents(oldVersion + 1, newName);
      }

      restoreCategory(oldName, oldVersion);
      redisService.delete(cacheKey);
    }
  }
  private List<CategoryChangeEvent> findNameChangedEvents(
    Long categoryVersion, String categoryName
  ) {
    return categoryChangeEventMapper.selectList(
      Wrappers.<CategoryChangeEvent>lambdaQuery()
        .eq(CategoryChangeEvent::getCategoryId, CATEGORY_ID)
        .eq(CategoryChangeEvent::getCategoryVersion, categoryVersion)
        .eq(CategoryChangeEvent::getEventType, "CATEGORY_NAME_CHANGED")
        .eq(
          CategoryChangeEvent::getPayload,
          "{\"name\":\"" + categoryName + "\"}"
        )
    );
  }

  private void deleteNameChangedEvents(
    Long categoryVersion, String categoryName
  ) {
    categoryChangeEventMapper.delete(
      Wrappers.<CategoryChangeEvent>lambdaQuery()
        .eq(CategoryChangeEvent::getCategoryId, CATEGORY_ID)
        .eq(CategoryChangeEvent::getCategoryVersion, categoryVersion)
        .eq(CategoryChangeEvent::getEventType, "CATEGORY_NAME_CHANGED")
        .eq(
          CategoryChangeEvent::getPayload,
          "{\"name\":\"" + categoryName + "\"}"
        )
    );
  }

  private void restoreCategory(String oldName, Long oldVersion) {
    Category dbCategory = categoryMapper.selectById(CATEGORY_ID);
    if (dbCategory != null) {
      dbCategory.setName(oldName);
      dbCategory.setCategoryVersion(oldVersion);
      categoryMapper.updateById(dbCategory);
    }
  }
}
