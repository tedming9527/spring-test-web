package org.example.springtestweb.category.service;

import org.example.springtestweb.category.bo.CategoryNameBo;
import org.example.springtestweb.category.entity.Category;
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

    Duration ttl = categories.isEmpty() ? Duration.ofMinutes(2) : Duration.ofMinutes(10);
    redisService.setObject(key, categories, ttl);
    return categories;
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
