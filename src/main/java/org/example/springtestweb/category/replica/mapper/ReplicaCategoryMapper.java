package org.example.springtestweb.category.replica.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.springtestweb.category.entity.Category;

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
  @Select("""
      SELECT id, category_version AS categoryVersion
      FROM goods_category
      WHERE id =#{id}
  """)
  Category findById(@Param("id") Long id);
}
