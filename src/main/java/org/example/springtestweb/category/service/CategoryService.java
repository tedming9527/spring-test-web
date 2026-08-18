package org.example.springtestweb.category.service;

import org.example.springtestweb.category.bo.CategoryNameBo;
import org.example.springtestweb.category.vo.CategoryVo;

import java.util.List;

public interface CategoryService {
  List<CategoryVo> findByParentId(Long parentId);
  boolean updateName(Long id, String name);
}
