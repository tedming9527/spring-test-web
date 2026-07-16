package org.example.springtestweb.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类 — 审计字段
 *
 * <p>所有需要记录创建/更新信息的表对应的 Entity 继承此类，
 * 配合 {@link org.example.springtestweb.config.MybatisMetaObjectHandler} 自动填充。
 */
@Data
public class BaseEntity {
  @TableField(fill = FieldFill.INSERT)
  private String creator;
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updater;
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
