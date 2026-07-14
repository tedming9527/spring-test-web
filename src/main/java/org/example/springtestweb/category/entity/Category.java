package org.example.springtestweb.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 商品分类表 (goods_category)
 */
@TableName("goods_category")
@Data
public class Category {

  /** 分类ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 分类名称 */
  private String name;

  /** 拼音 */
  private String spell;

  /** 是否叶子节点（列名 is_leaf，字段名与列名不一致需 @TableField 映射） */
  @TableField("is_leaf")
  private Boolean leaf;

  /** 父分类ID */
  private Long parentId;

  /** 权重 */
  private Integer weight;

  /** 是否隐藏 */
  private Boolean hidden;

  /** 图标URL */
  private String iconUrl;

  /** 是否便民服务类型 */
  private Boolean convenient;

  /** 商品数量 */
  private Integer itemNumber;

  /** 保证金 */
  private BigDecimal margin;

  /** 居间服务费率 (0~1) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal categoryRate;

  /** 募集比率 (0~1) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal donateRate;

  /** 税率 (0~1) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal taxRate;

  /** 默认管家激励分成比例 (0~1) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal p1f1RateKeeper;

  /** 大区管家激励比率 (0~1，默认 0.4) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal p1f1Rate;

  /** 总部管家激励比率 (0~1，默认 0) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal p2f2Rate;

  /** 总部管家激励默认分成比例 (0~1) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal p2f2RateKeeper;

  /** 总部分成比率 (0~1，总部+大区=1，默认 0.5) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal profitHeadquarterRate;

  /** 大区拉新激励比例 (0~1，默认 0) */
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private BigDecimal newUserIncentiveRate;

  // ---- 非数据库字段（@TableField(exist = false)） ----

  /** 顶部图片（不入表） */
  @TableField(exist = false)
  private String topPic;

  /** 配置是否锁定（不入表） */
  @TableField(exist = false)
  private Boolean isConfigLock;

  /** 配置ID（不入表） */
  @TableField(exist = false)
  private Long configId;

  /** 展示权重（不入表） */
  @TableField(exist = false)
  private Integer showWeight;

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (id == null ? 0 : id.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Category category = (Category) o;
    return Objects.equals(id, category.id);
  }
}
