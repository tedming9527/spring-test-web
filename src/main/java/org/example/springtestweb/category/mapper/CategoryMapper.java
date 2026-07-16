package org.example.springtestweb.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.vo.CategoryVo;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {
  List<CategoryVo> findByParentId(Long parentId);
}
