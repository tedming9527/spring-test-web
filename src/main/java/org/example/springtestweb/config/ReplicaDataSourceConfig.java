package org.example.springtestweb.config;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class ReplicaDataSourceConfig {

  @Bean(defaultCandidate = false)
  @Qualifier("replica")
  @ConfigurationProperties("app.datasource.replica")
  public DataSourceProperties replicaDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "replicaDataSource", defaultCandidate = false)
  @Qualifier("replica")
  @ConfigurationProperties("app.datasource.replica.configuration")
  public HikariDataSource replicaDataSource(
      @Qualifier("replica") DataSourceProperties dataSourceProperties
  ) {
    return dataSourceProperties.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

  @Bean
  @Qualifier("replica")
  public JdbcTemplate replicaJdbcTemplate(@Qualifier("replica") DataSource replicaDataSource) {
    return new JdbcTemplate(replicaDataSource);
  }

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
