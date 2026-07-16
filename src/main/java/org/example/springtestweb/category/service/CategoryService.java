package org.example.springtestweb.category.service;

import org.example.springtestweb.category.vo.CategoryVo;

import java.util.List;

public interface CategoryService {
  List<CategoryVo> findByParentId(Long parentId);
}
