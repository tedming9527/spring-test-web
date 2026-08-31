package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.category.vo.CategoryVo;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private RedisService redisService;
  @Autowired
  private CategoryTransactionalService categoryTransactionalService;

  private List<CategoryVo> getCachedCategory(String key) {
    return redisService.getList(key, CategoryVo.class);
  }

  @Override
  public List<CategoryVo> findByParentId(Long parentId) {
    String cacheKey = "category:children:" + parentId;
    String lockKey = "lock:" + cacheKey;
    long waitDeadline = System.nanoTime() + Duration.ofMillis(900).toNanos();
    long maxSleepNanos = Duration.ofMillis(300).toNanos();
    while (System.nanoTime() < waitDeadline) {
      // 获取缓存
      List<CategoryVo> cached = getCachedCategory(cacheKey);
      if (cached != null) {
        return cached;
      }
      String uuid = UUID.randomUUID().toString();
      if (redisService.lock(lockKey, uuid)) {
        try {
          List<CategoryVo> cachedAfterLocked = getCachedCategory(cacheKey);
          if (cachedAfterLocked != null) {
            return cachedAfterLocked;
          }
          List<CategoryVo> categories = categoryMapper.findByParentId(parentId);
          Duration ttl = categories.isEmpty() ? Duration.ofMinutes(2): Duration.ofMinutes(10);
          redisService.setObject(cacheKey, categories, ttl);
          return categories;
        } finally {
          redisService.unlock(lockKey, uuid);
        }
      } else {
        long remainingNanos = waitDeadline - System.nanoTime();
        if (remainingNanos <= 0) {
          break;
        }
        long sleepNanos = Math.min(maxSleepNanos, remainingNanos);


        try {
          TimeUnit.NANOSECONDS.sleep(sleepNanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("等待分类缓存重建时被中断", e);
        }
      }
    }
    List<CategoryVo> cachedAfterTimeout = getCachedCategory(cacheKey);
    if (cachedAfterTimeout != null) {
      return cachedAfterTimeout;
    }
    throw new IllegalStateException("等待分类缓存重建超时");
  }

  @Override
  public boolean updateName(Long id, String name) {
    Category category =  categoryMapper.selectById(id);
    if (category == null) {
      return false;
    }
    category.setName(name);

    String cacheKey = "category:children:" + category.getParentId();
    String lockKey = "lock:" + cacheKey;
    long waitDeadline = System.nanoTime() + Duration.ofMillis(900).toNanos();
    long maxSleepNanos = Duration.ofMillis(300).toNanos();
    String uuid = UUID.randomUUID().toString();
    while (System.nanoTime() < waitDeadline) {
      if (redisService.lock(lockKey, uuid)) {
        try {
          return categoryTransactionalService.updateName(category, cacheKey);
        } finally {
          redisService.unlock(lockKey, uuid);
        }
      } else {
        long remainingNanos = waitDeadline - System.nanoTime();
        if (remainingNanos <= 0) {
          break;
        }
        long sleepNanos = Math.min(maxSleepNanos, remainingNanos);
        try {
          TimeUnit.NANOSECONDS.sleep(sleepNanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("等待修改分类时被中断", e);
        }
      }
    }
    throw new IllegalStateException("分类正在更新，请稍后重试");
  }
}
