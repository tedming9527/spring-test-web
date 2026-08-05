package org.example.springtestweb.redis.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Data
public class RedisService {
  private final StringRedisTemplate stringRedisTemplate;
  public RedisService(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * 写入字符串并设置过期时间
   * @param key
   * @param value
   * @param timeout
   */
  public void set(String key, String value, Duration timeout) {
    stringRedisTemplate.opsForValue().set(key, value, timeout);
  }

  /**
   * 根据键读取字符串
   * @param key
   * @return
   */
  public String get(String key) {
    return stringRedisTemplate.opsForValue().get(key);
  }

  /**
   * 删除指定键
   * @param key
   * @return
   */
  public Boolean delete(String key) {
    return stringRedisTemplate.delete(key);
  }
}
