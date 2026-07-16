package org.example.springtestweb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "org.example.springtestweb", markerInterface = BaseMapper.class)
public class SpringTestWebApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringTestWebApplication.class, args);
  }

}
