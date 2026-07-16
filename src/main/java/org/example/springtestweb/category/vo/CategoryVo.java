package org.example.springtestweb.category.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVo implements Serializable {
  /** 分类ID */
  private Long id;
  /** 分类名称 */
  private String name;
  /** 拼音 */
  private String spell;
  /** 是否叶子节点 */
  private Boolean leaf;
  /** 父分类ID */
  private Long parentId;
  /** 权重 */
  private Integer weight;
  /** 商品数量 */
  private Integer itemNumber;
  /** 居间服务费率 */
  private BigDecimal categoryRate;
  /** 募集比率 */
  private BigDecimal donateRate;
  /** 税率 */
  private BigDecimal taxRate;
  /** 是否隐藏 */
  private Boolean hidden;
  /** 图标URL */
  private String iconUrl;
  /** 保证金 */
  private BigDecimal margin;
  /** 大区管家激励比率 */
  private BigDecimal p1f1Rate;
  /** 默认管家激励分成比例 */
  private BigDecimal p1f1RateKeeper;
  /** 总部管家激励比率 */
  private BigDecimal p2f2Rate;
  /** 总部分成比率 */
  private BigDecimal profitHeadquarterRate;
  /** 大区拉新激励比例 */
  private BigDecimal newUserIncentiveRate;

  /** 一级分类ID */
  private Long topId;
  /** 二级分类ID */
  private Long secId;
  /** 三级分类ID */
  private Long trdId;
  /** 是否有干洗图 */
  private boolean dryCleanPic;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新时间 */
  private LocalDateTime updateTime;

  /** 子分类列表 */
  private List<CategoryVo> children;
}
