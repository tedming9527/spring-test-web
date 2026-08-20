package org.example.springtestweb.category.service;

import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.category.vo.CategoryVo;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

import static java.lang.Thread.sleep;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private RedisService redisService;

  private List<CategoryVo> getCachedCategory(String key) {
    return redisService.getList(key, CategoryVo.class);
  }

  @Override
  public List<CategoryVo> findByParentId(Long parentId) {
    String cacheKey = "category:children:" + parentId;
    String lockKey = "lock:" + cacheKey;
    for (int attempt = 0; attempt < 3; attempt++) {
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
        try {
          sleep(300);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("等待分类缓存重建时被中断", e);
        }
      }
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
    int affectRows = categoryMapper.updateById(category);
    if (affectRows != 1) {
      return false;
    }
    String key = "category:children:" + category.getParentId();
    redisService.delete(key);
    return true;
  }
}
