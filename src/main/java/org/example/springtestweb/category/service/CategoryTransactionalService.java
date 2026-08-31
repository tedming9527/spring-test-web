package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CategoryTransactionalService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private RedisService redisService;

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
   int affectRows = categoryMapper.updateById(category);
    if (affectRows != 1) {
      return false;
    }
    databaseUpdated.set(true);
    return true;
  }
}
