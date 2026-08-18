package org.example.springtestweb.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.springtestweb.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods")
public class Goods extends BaseEntity {
  @TableId(type= IdType.AUTO)
  private Long id;
  private String name;
  private Long categoryId;
  private Long priceCent;
  private Integer stock;
  private String description;
}
