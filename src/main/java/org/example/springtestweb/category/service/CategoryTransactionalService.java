package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.entity.CategoryChangeEvent;
import org.example.springtestweb.category.mapper.CategoryChangeEventMapper;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CategoryTransactionalService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private RedisService redisService;
  @Autowired
  private CategoryChangeEventMapper categoryChangeEventMapper;
  @Autowired
  private ObjectMapper objectMapper;

  @Transactional
  public Boolean updateName(Category category, String cacheKey) {
    AtomicBoolean databaseUpdated = new AtomicBoolean(false);
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        if (databaseUpdated.get()) {
          redisService.delete(cacheKey);
        }
      }
    });
    Long expectedCurrentVersion = category.getCategoryVersion();
    if (expectedCurrentVersion == null) {
      throw new IllegalStateException("分类版本不能为空");
    }

    int affectRows = categoryMapper.updateNameIfVersionMatches(
      category.getId(),
      category.getName(),
      expectedCurrentVersion
    );
    if (affectRows != 1) {
      return false;
    }
    category.setCategoryVersion(expectedCurrentVersion + 1);
    CategoryChangeEvent event = new CategoryChangeEvent();
    event.setCategoryId(category.getId());
    event.setCategoryVersion(category.getCategoryVersion());
    event.setEventType("CATEGORY_NAME_CHANGED");
    event.setPayload(toNameChangedPayload(category.getName()));
    int insertedRows = categoryChangeEventMapper.insert(event);
    if (insertedRows != 1) {
      throw new IllegalStateException("分类变更事件创建失败");
    }

    databaseUpdated.set(true);
    return true;
  }
  private String toNameChangedPayload(String name) {
    try {
      return objectMapper.writeValueAsString(Map.of("name", name));
    } catch (JacksonException e) {
      throw new IllegalStateException("分类变更事件序列化失败", e);
    }
  }
}
