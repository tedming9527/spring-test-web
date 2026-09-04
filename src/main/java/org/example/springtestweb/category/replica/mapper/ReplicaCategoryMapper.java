package org.example.springtestweb.category.replica.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.vo.CategoryVo;

import java.util.List;

public interface ReplicaCategoryMapper {
  @Update("""
      UPDATE goods_category
      SET name = #{name},
          category_version = #{eventVersion}
      WHERE id =#{id}
          AND category_version < #{eventVersion}
  """)
  int syncReplicaNameIfVersionMatches(
    @Param("id") Long id,
    @Param("name") String name,
    @Param("eventVersion") Long eventVersion
  );
}
