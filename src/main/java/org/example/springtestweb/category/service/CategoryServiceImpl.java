package org.example.springtestweb.category.service;

import org.example.springtestweb.category.bo.CategoryNameBo;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.category.vo.CategoryVo;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Autowired
  private RedisService redisService;

  @Override
  public List<CategoryVo> findByParentId(Long parentId) {
    String key = "category:children:" + parentId;
    List<CategoryVo> cachedCategories = redisService.getList(key, CategoryVo.class);
    if (cachedCategories != null) {
      return cachedCategories;
    }
    List<CategoryVo> categories = categoryMapper.findByParentId(parentId);

    Duration ttl = categories.isEmpty() ? Duration.ofMinutes(1) : Duration.ofMinutes(6);
    redisService.setObject(key, categories, ttl);
    return categories;
  }
}
