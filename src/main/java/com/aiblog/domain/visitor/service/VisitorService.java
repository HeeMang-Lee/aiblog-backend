package com.aiblog.domain.visitor.service;

import com.aiblog.domain.visitor.dto.VisitorStatsResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class VisitorService {

  private static final String KEY_TOTAL = "visitor:total";
  private static final String KEY_TODAY_PREFIX = "visitor:today:";
  private static final String KEY_POST_PREFIX = "visitor:post:";
  private static final String KEY_IP_SUFFIX = ":ips";
  private static final String KEY_TRACKED_POSTS = "visitor:tracked-posts";
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  private final StringRedisTemplate redisTemplate;
  private final long ipTtlHours;

  public VisitorService(
      StringRedisTemplate redisTemplate,
      @Value("${app.visitor.ip-ttl-hours}") long ipTtlHours) {
    this.redisTemplate = redisTemplate;
    this.ipTtlHours = ipTtlHours;
  }

  public void recordVisit(Long postId, String clientIp) {
    String ipKey = KEY_POST_PREFIX + postId + KEY_IP_SUFFIX;

    Long added = redisTemplate.opsForSet().add(ipKey, clientIp);
    if (added != null && added > 0) {
      Long ttl = redisTemplate.getExpire(ipKey);
      if (ttl != null && ttl == -1) {
        redisTemplate.expire(ipKey, ipTtlHours, TimeUnit.HOURS);
      }
      redisTemplate.opsForValue().increment(KEY_POST_PREFIX + postId);
      redisTemplate.opsForValue().increment(KEY_TOTAL);
      String today = todayKey();
      redisTemplate.opsForValue().increment(today);
      redisTemplate.expire(today, 48, TimeUnit.HOURS);
      redisTemplate.opsForSet().add(KEY_TRACKED_POSTS, String.valueOf(postId));
    }
  }

  public VisitorStatsResponse getVisitorStats() {
    long total = toLong(redisTemplate.opsForValue().get(KEY_TOTAL));
    long today = toLong(redisTemplate.opsForValue().get(todayKey()));
    return new VisitorStatsResponse(total, today);
  }

  public long getPostViewCount(Long postId) {
    return toLong(redisTemplate.opsForValue().get(KEY_POST_PREFIX + postId));
  }

  public Set<String> getTrackedPostIds() {
    Set<String> ids = new HashSet<>();
    ScanOptions options = ScanOptions.scanOptions().count(100).build();
    try (Cursor<String> cursor = redisTemplate.opsForSet()
        .scan(KEY_TRACKED_POSTS, options)) {
      while (cursor.hasNext()) {
        ids.add(cursor.next());
      }
    }
    return ids;
  }

  public void removeTrackedPost(Long postId) {
    redisTemplate.opsForSet()
        .remove(KEY_TRACKED_POSTS, String.valueOf(postId));
    redisTemplate.delete(KEY_POST_PREFIX + postId);
    redisTemplate.delete(KEY_POST_PREFIX + postId + KEY_IP_SUFFIX);
  }

  private String todayKey() {
    return KEY_TODAY_PREFIX + LocalDate.now().format(DATE_FORMAT);
  }

  private long toLong(String value) {
    return value != null ? Long.parseLong(value) : 0;
  }
}
