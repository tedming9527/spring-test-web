package org.example.springtestweb.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@MapperScan(
  basePackages = "org.example.springtestweb.category.replica.mapper",
  sqlSessionFactoryRef = "replicaSqlSessionFactory"
)
public class ReplicaMybatisConfig {
  @Bean(name = "replicaSqlSessionFactory")
  public SqlSessionFactory replicaSqlSessionFactory(
    @Qualifier("replica") DataSource replicaDataSource
  ) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(replicaDataSource);
    return factory.getObject();
  }
}