package org.example.springtestweb.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 从库 {@code springtestweb_replica} 的 Flyway 迁移配置。
 */
@Configuration(proxyBeanMethods = false)
public class ReplicaFlywayConfig {

  @Bean(name = "replicaFlyway", initMethod = "migrate")
  public Flyway replicaFlyway(@Qualifier("replica") DataSource replicaDataSource) {
    return Flyway.configure()
        .dataSource(replicaDataSource)
        .locations("classpath:db/replica/migration")
        .baselineOnMigrate(true)
        .baselineVersion("20260901")
        .baselineDescription("Replica schema before Flyway management")
        .load();
  }
}
