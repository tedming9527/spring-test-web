package org.example.springtestweb.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.springtestweb.entity.BaseEntity;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@TableName("category_change_event")
@Data
public class CategoryChangeEvent extends BaseEntity {
  @TableId(type=IdType.AUTO)
  private Long id;
  private Long categoryId;
  private Long categoryVersion;
  private String eventType;
  private String payload;
  private String status;
  private Integer retryCount;
  private String lastError;
  private LocalDateTime nextRetryAt;
}
