package org.example.springtestweb.category.replica.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
