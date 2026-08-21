package org.example.springtestweb.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import org.example.springtestweb.redis.dto.UserCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static java.util.UUID.*;

@Service
@Data
public class RedisService {
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;
  private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
      """
      if redis.call('get', KEYS[1]) == ARGV[1] then
          return redis.call('del', KEYS[1])
      else
         return 0
      end
      """,
      Long.class
  );
  public RedisService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.objectMapper = objectMapper;
  }

  public void setObject(String key, Object value, Duration duration) {
    try {

    String json = objectMapper.writeValueAsString(value);
    stringRedisTemplate.opsForValue().set(key, json, duration);
    } catch (JacksonException e) {
      throw new IllegalStateException("Redis序列化失败",e);
    }
  }
  public <T> T getObject(String key, Class<T> clazz) {
    String json = stringRedisTemplate.opsForValue().get(key);
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, clazz);
    }  catch (JacksonException e) {
      throw new IllegalStateException("Redis反序列化失败", e);
    }
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
  public void putHash(String key, String field, String value) {
    stringRedisTemplate.opsForHash().put(key, field, value);
  }
  public Object getHash(String key, String field) {
    return stringRedisTemplate.opsForHash().get(key, field);
  }
  public Map<Object, Object> getHash(String key) {
    return stringRedisTemplate.opsForHash().entries(key);
  }
  public Long deleteHashField(String key, String field) {
    return stringRedisTemplate.opsForHash().delete(key, field);
  }
  public Boolean expire(String key, Duration timeout) {
    return stringRedisTemplate.expire(key, timeout);
  }
  public Long getExpire(String key) {
    return stringRedisTemplate.getExpire(key);
  }
  public Long pushQueue(String key, String value) {
    return stringRedisTemplate.opsForList().rightPush(key, value);
  }
  public String popQueue(String key) {
    return stringRedisTemplate.opsForList().leftPop(key);
  }
  public List<String> getQueue(String key) {
    return stringRedisTemplate.opsForList().range(key, 0, -1);
  }
  public Long getQueueSize(String key) {
    return stringRedisTemplate.opsForList().size(key);
  }
  public Long addSetMembers(String key, String... value) {
    return stringRedisTemplate.opsForSet().add(key, value);
  }
  public Set<String> getSetMembers(String key) {
    return stringRedisTemplate.opsForSet().members(key);
  }
  public Boolean isSetMember(String key, String value) {
    return stringRedisTemplate.opsForSet().isMember(key, value);
  }
  public Long removeSetMembers(String key, String... value) {
    return stringRedisTemplate.opsForSet().remove(key, (Object[]) value);
  }
  public Long getSetSize(String key) {
    return stringRedisTemplate.opsForSet().size(key);
  }
  public Boolean addZSetMembers(String key, String value, double score) {
    return stringRedisTemplate.opsForZSet().add(key, value, score);
  }
  public Set<ZSetOperations.TypedTuple<String>> getZSetRanking(String key) {
    return stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, -1);
  }
  public Long getZSetMemberRank(String key, String member) {
    return stringRedisTemplate.opsForZSet().reverseRank(key, member);
  }
  public Double getZSetMemberScore(String key, String member) {
    return stringRedisTemplate.opsForZSet().score(key, member);
  }
  public Double incrementZSetMemberScore(String key, String member, Double increment) {
    return stringRedisTemplate.opsForZSet().incrementScore(key, member, increment);
  }
  public Long removeZSetMember(String key, String member) {
    return stringRedisTemplate.opsForZSet().remove(key, member);
  }
  public <T> List<T> getList(String key, Class<T> clazz) {
    String json = stringRedisTemplate.opsForValue().get(key);
    if (json == null) {
      return null;
    }
    try {
      return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    } catch (JacksonException e) {
      throw new IllegalStateException("Redis 列表反序列化失败，key=" + key, e);
    }
  }
  public boolean lock(String lockKey, String uuid) {
    return stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, Duration.ofMillis(500));
  }
  public void unlock(String lockKey, String uuid) {
    stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), uuid);
  }
}
