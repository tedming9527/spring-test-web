package org.example.springtestweb.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.example.springtestweb.context.RequestUserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * <p>配合 {@link com.baomidou.mybatisplus.annotation.FieldFill} 注解，
 * 在插入/更新时自动填充 {@link org.example.springtestweb.entity.BaseEntity} 的审计字段。
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {
  @Override
  public void insertFill(MetaObject metaObject) {
    this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
    this.setFieldValByName("creator", getCurrentUserId(), metaObject);
    this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    this.setFieldValByName("updater", getCurrentUserId(), metaObject);
  }
  @Override
  public void updateFill(MetaObject metaObject) {
    this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    this.setFieldValByName("updater", getCurrentUserId(), metaObject);
  }

  private String getCurrentUserId() {
    String userId = RequestUserContext.getUserId();
    if (userId != null && !userId.isBlank()) {
      return userId;
    }
    return "system";
  }
}
