package org.example.springtestweb.category.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.springtestweb.category.entity.Category;
import org.example.springtestweb.category.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplicaCategoryBootstrapService {

  private static final String INSERT_IGNORE_SQL = """
      INSERT IGNORE INTO goods_category (
          id, name, spell, is_leaf, parent_id, weight, hidden, icon_url, convenient,
          item_number, margin, category_rate, donate_rate, tax_rate, p1f1_rate_keeper,
          p1f1_rate, p2f2_rate, p2f2_rate_keeper, profit_headquarter_rate,
          new_user_incentive_rate, creator, updater, create_time, update_time, category_version
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  private final CategoryMapper categoryMapper;
  private final JdbcTemplate replicaJdbcTemplate;

  public ReplicaCategoryBootstrapService(
      CategoryMapper categoryMapper,
      @Qualifier("replica") JdbcTemplate replicaJdbcTemplate
  ) {
    this.categoryMapper = categoryMapper;
    this.replicaJdbcTemplate = replicaJdbcTemplate;
  }

  public int initializeGoodsCategories() {
    List<Category> categories = categoryMapper.selectList(
        Wrappers.<Category>lambdaQuery().orderByAsc(Category::getId)
    );
    int[][] results = replicaJdbcTemplate.batchUpdate(
        INSERT_IGNORE_SQL,
        categories,
        categories.size(),
        (statement, category) -> {
          statement.setObject(1, category.getId());
          statement.setObject(2, category.getName());
          statement.setObject(3, category.getSpell());
          statement.setObject(4, category.getLeaf());
          statement.setObject(5, category.getParentId());
          statement.setObject(6, category.getWeight());
          statement.setObject(7, category.getHidden());
          statement.setObject(8, category.getIconUrl());
          statement.setObject(9, category.getConvenient());
          statement.setObject(10, category.getItemNumber());
          statement.setObject(11, category.getMargin());
          statement.setObject(12, category.getCategoryRate());
          statement.setObject(13, category.getDonateRate());
          statement.setObject(14, category.getTaxRate());
          statement.setObject(15, category.getP1f1RateKeeper());
          statement.setObject(16, category.getP1f1Rate());
          statement.setObject(17, category.getP2f2Rate());
          statement.setObject(18, category.getP2f2RateKeeper());
          statement.setObject(19, category.getProfitHeadquarterRate());
          statement.setObject(20, category.getNewUserIncentiveRate());
          statement.setObject(21, category.getCreator());
          statement.setObject(22, category.getUpdater());
          statement.setObject(23, category.getCreateTime());
          statement.setObject(24, category.getUpdateTime());
          statement.setObject(25, category.getCategoryVersion());
        }
    );
    Integer replicaCount = replicaJdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM goods_category", Integer.class
    );
    if (replicaCount == null || replicaCount != categories.size()) {
      throw new IllegalStateException("从库分类数量与主库不一致");
    }
    return (int) java.util.Arrays.stream(results)
        .flatMapToInt(java.util.Arrays::stream)
        .filter(result -> result > 0)
        .count();
  }
}
