package com.aiblog.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

public class TestDataSetup {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void execute(JdbcTemplate jdbcTemplate, String jsonPath) {
    try {
      InputStream inputStream = TestDataSetup.class.getClassLoader()
          .getResourceAsStream(jsonPath);
      if (inputStream == null) {
        throw new IllegalArgumentException(
            "테스트 데이터 파일을 찾을 수 없습니다: " + jsonPath);
      }
      String json = new String(inputStream.readAllBytes(),
          StandardCharsets.UTF_8);
      JsonNode root = objectMapper.readTree(json);

      for (String tableName : root.propertyNames()) {
        JsonNode rows = root.get(tableName);
        for (JsonNode row : rows) {
          insertRow(jdbcTemplate, tableName, row);
        }
        resetIdentity(jdbcTemplate, tableName);
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "테스트 데이터 로드 실패: " + jsonPath, e);
    }
  }

  public static void truncateAll(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
    jdbcTemplate.execute("TRUNCATE TABLE ai_result");
    jdbcTemplate.execute("TRUNCATE TABLE post_recommendation");
    jdbcTemplate.execute("TRUNCATE TABLE attachment");
    jdbcTemplate.execute("TRUNCATE TABLE post");
    jdbcTemplate.execute("TRUNCATE TABLE category");
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
  }

  private static void insertRow(JdbcTemplate jdbcTemplate,
      String tableName, JsonNode row) {
    List<String> columns = new ArrayList<>();
    List<Object> values = new ArrayList<>();

    for (String field : row.propertyNames()) {
      columns.add(field);
      JsonNode value = row.get(field);
      if (value.isNull()) {
        values.add(null);
      } else if (value.isInt()) {
        values.add(value.intValue());
      } else if (value.isLong()) {
        values.add(value.longValue());
      } else {
        values.add(value.asText());
      }
    }

    String sql = "INSERT INTO " + tableName + " ("
        + String.join(", ", columns) + ") VALUES ("
        + columns.stream().map(c -> "?")
            .collect(Collectors.joining(", "))
        + ")";

    jdbcTemplate.update(sql, values.toArray());
  }

  private static void resetIdentity(JdbcTemplate jdbcTemplate,
      String tableName) {
    Long maxId = jdbcTemplate.queryForObject(
        "SELECT COALESCE(MAX(id), 0) FROM " + tableName, Long.class);
    if (maxId != null && maxId > 0) {
      jdbcTemplate.execute(
          "ALTER TABLE " + tableName
              + " ALTER COLUMN id RESTART WITH " + (maxId + 1));
    }
  }
}
