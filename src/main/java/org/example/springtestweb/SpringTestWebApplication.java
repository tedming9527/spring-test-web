package org.example.springtestweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.springtestweb")
public class SpringTestWebApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringTestWebApplication.class, args);
  }

}
