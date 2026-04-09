package org.example.springtestweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @GetMapping("/hello")
  public String hello() {
    return "Hello Spring Boot3, I use GitHub Actions to CI/CD!";
  }
}
