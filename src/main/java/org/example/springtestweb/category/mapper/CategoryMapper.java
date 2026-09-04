package org.example.springtestweb.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.vo.CategoryVo;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {
  List<CategoryVo> findByParentId(Long parentId);
  @Update("""
      UPDATE goods_category
      SET name = #{name},
          category_version = category_version + 1
      WHERE id = #{id}
          AND category_version = #{expectedCurrentVersion}
  """)
  int updateNameIfVersionMatches(
      @Param("id") Long id,
      @Param("name") String name,
      @Param("expectedCurrentVersion") Long expectedCurrentVersion
  );
}
