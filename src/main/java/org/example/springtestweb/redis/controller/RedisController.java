package org.example.springtestweb.redis.controller;

import org.example.springtestweb.redis.dto.UserCache;
import org.example.springtestweb.redis.service.RedisService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
  @PostMapping("/hash/{key}/{field}")
  public String putHash(@PathVariable String key, @PathVariable String field, @RequestParam String value) {
    redisService.putHash(key, field, value);
    return "hash 字段写入成功";
  }
  @GetMapping("/hash/{key}/{field}")
  public ResponseEntity<Object> getHash(@PathVariable String key, @PathVariable String field) {
    Object value = redisService.getHash(key, field);
    if (value != null) {
      return ResponseEntity.ok(value);
    }
    return ResponseEntity.notFound().build();
  }
  @GetMapping("/hash/{key}")
  public ResponseEntity<Object> getHash(@PathVariable String key) {
    Object value = redisService.getHash(key);
    if (value != null) {
      return ResponseEntity.ok(value);
    }
    return ResponseEntity.notFound().build();
  }
  @DeleteMapping("/hash/{key}/{field}")
  public String deleteHashField(@PathVariable String key, @PathVariable String field) {
    Long deleted = redisService.deleteHashField(key, field);
    return deleted > 0 ? "Hash 字段删除成功" : "Hash 字段不存在";
  }
  @PutMapping("/hash/{key}/expire")
  public String expireHash(@PathVariable String key, @RequestParam long timeout) {
    Boolean success = redisService.expire(key, Duration.ofSeconds(timeout));
    return Boolean.TRUE.equals(success) ? "Hash 过期时间设置成功" : "Hash不存在";
  }
  @GetMapping("/hash/{key}/ttl")
  public Long getHashTTL(@PathVariable String key) {
    return redisService.getExpire(key);
  }
  @PostMapping("/list/{key}")
  public Long pushQueue(@PathVariable String key, @RequestParam String value) {
    return redisService.pushQueue(key, value);
  }
  @DeleteMapping("/list/{key}/first")
  public ResponseEntity<String> popQueue(@PathVariable String key) {
    String value = redisService.popQueue(key);
    if (value != null) {
      return ResponseEntity.ok(value);
    }
    return ResponseEntity.notFound().build();
  }
  @GetMapping("/list/{key}")
  public List<String> getQueue(@PathVariable String key) {
    return redisService.getQueue(key);
  }
  @GetMapping("/list/{key}/size")
  public Long getQueueSize(@PathVariable String key) {
    return redisService.getQueueSize(key);
  }
  @PostMapping("/set/{key}")
  public Long addSetMember(@PathVariable String key, @RequestParam String value) {
    return  redisService.addSetMembers(key, value);
  }
  @GetMapping("/set/{key}")
  public Set<String> getSetMembers(@PathVariable String key) {
    return redisService.getSetMembers(key);
  }
  @GetMapping("/set/{key}/contains")
  public Boolean isSetMember(@PathVariable String key, @RequestParam String value) {
    return redisService.isSetMember(key, value);
  }
  @DeleteMapping("/set/{key}")
  public Long removeSetMembers(@PathVariable String key, @RequestParam String value) {
    return redisService.removeSetMembers(key, value);
  }
  @GetMapping("/set/{key}/size")
  public Long getSetSize(@PathVariable String key) {
    return redisService.getSetSize(key);
  }
  @PostMapping("/zset/{key}")
  public Boolean addZSetMember(@PathVariable String key, @RequestParam String member, @RequestParam double score) {
    return redisService.addZSetMembers(key, member, score);
  }
  @GetMapping("/zset/{key}/ranking")
  public Set<ZSetOperations.TypedTuple<String>> getZSetRanking(@PathVariable String key) {
    return redisService.getZSetRanking(key);
  }
  @GetMapping("/zset/{key}/rank")
  public Long getZSetMemberRank(@PathVariable String key, @RequestParam String member) {
    return redisService.getZSetMemberRank(key, member);
  }
  @GetMapping("/zset/{key}/score")
  public Double getZSetMemberScore(@PathVariable String key, @RequestParam String member) {
    return redisService.getZSetMemberScore(key, member);
  }

  @DeleteMapping("/zset/{key}")
  public Long removeZSetMember(@PathVariable String key, @RequestParam String member) {
    return redisService.removeZSetMember(key, member);
  }
  @PostMapping("/object/user/{id}")
  public String setUserCache(@PathVariable Long id, @RequestBody UserCache user, @RequestParam(defaultValue = "600") long ttlSeconds) {
    String key = "user:cache:" + id;
    redisService.setObject(key, user, Duration.ofSeconds(ttlSeconds));
    return "用户对象缓存成功";
  }
  public ResponseEntity<UserCache> getUserCache(@PathVariable Long id) {
    String key = "user:cache:" + id;
    UserCache user = redisService.getObject(key, UserCache.class);
    if (user != null) {
      return ResponseEntity.ok(user);
    }
    return ResponseEntity.notFound().build();
  }
}
