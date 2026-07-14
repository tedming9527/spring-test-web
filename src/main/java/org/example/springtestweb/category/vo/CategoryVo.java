package org.example.springtestweb.category.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CategoryVo implements Serializable {
  private Long id;
  private String name;
  private String spell;
  private Integer weight;
  private Integer itemNumber;
  private Long parentId;
  private Boolean leaf;
  private BigDecimal categoryRate;
  private BigDecimal donateRate;
  private BigDecimal taxRate;
  private BigDecimal p1f1Rate;
  private BigDecimal p1f1RateKeeper;
  private BigDecimal p2f2Rate;
  private BigDecimal profitHeadquarterRate;
  private BigDecimal newUserIncentiveRate;
  private String iconUrl;
  private Boolean hidden;
  private BigDecimal margin;

  private Long topId;
  private Long secId;
  private Long trdId;
  private boolean dryCleanPic;

  private List<CategoryVo> children;
}
