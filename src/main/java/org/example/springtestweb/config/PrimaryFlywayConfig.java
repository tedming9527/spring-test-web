package org.example.springtestweb.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 主库 {@code springtestweb} 的 Flyway 迁移配置。
 *
 * <p>从库迁移使用独立的 {@link ReplicaFlywayConfig}。显式配置主库迁移，避免从库 Flyway
 * Bean 存在时 Spring Boot 自动配置退让，导致主库迁移未执行。
 */
@Configuration(proxyBeanMethods = false)
public class PrimaryFlywayConfig {

  @Bean(name = "primaryFlyway", initMethod = "migrate")
  public Flyway primaryFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .baselineVersion("20260515")
        .baselineDescription("Baseline after V20260515 has been applied")
        .load();
  }
}
