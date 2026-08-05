package org.example.springtestweb.redis.controller;

import org.example.springtestweb.redis.service.RedisService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/redis")
public class RedisController {
  private final RedisService redisService;
  public RedisController(RedisService redisService) {
    this.redisService = redisService;
  }
  @PostMapping("/{key}")
  public String set(@PathVariable String key, @RequestParam String value, @RequestParam(defaultValue = "300") long ttlSeconds) {
    redisService.set(key, value, Duration.ofSeconds(ttlSeconds));
    return "Redis 写入成功";
  }
  @GetMapping("/{key}")
  public String get(@PathVariable String key) {
    return redisService.get(key);
  }
  @DeleteMapping("/{key}")
  public String delete(@PathVariable String key) {
    Boolean deleted = redisService.delete(key);
    return Boolean.TRUE.equals(deleted) ? "Redis 删除成功" : "Redis 键不存在";
  }
}
