package com.aiblog.domain.ai.cache;

import com.aiblog.domain.ai.entity.AiAgentType;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiCacheService {

  private static final String KEY_PREFIX = "ai";

  private final StringRedisTemplate redisTemplate;
  private final long ttlHours;

  public AiCacheService(
      StringRedisTemplate redisTemplate,
      @Value("${app.ai.cache.ttl-hours}") long ttlHours) {
    this.redisTemplate = redisTemplate;
    this.ttlHours = ttlHours;
  }

  public Optional<String> get(AiAgentType agentType, Long postId,
      String contentHash) {
    String key = buildKey(agentType, postId, contentHash);
    String value = redisTemplate.opsForValue().get(key);
    return Optional.ofNullable(value);
  }

  public void set(AiAgentType agentType, Long postId, String contentHash,
      String content) {
    String key = buildKey(agentType, postId, contentHash);
    redisTemplate.opsForValue().set(key, content, ttlHours, TimeUnit.HOURS);
  }

  private String buildKey(AiAgentType agentType, Long postId,
      String contentHash) {
    return KEY_PREFIX + ":"
        + agentType.name().toLowerCase() + ":"
        + postId + ":"
        + contentHash;
  }
}
