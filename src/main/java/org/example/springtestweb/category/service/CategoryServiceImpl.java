package org.example.springtestweb.category.service;

import org.example.springtestweb.category.mapper.CategoryMapper;
import org.example.springtestweb.category.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
  @Autowired
  private CategoryMapper categoryMapper;
  @Override
  public List<CategoryVo> findByParentId(Long parentId) {
    return categoryMapper.findByParentId(parentId);
  }
}
