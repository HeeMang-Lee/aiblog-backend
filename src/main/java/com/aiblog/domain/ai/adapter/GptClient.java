package com.aiblog.domain.ai.adapter;

import com.aiblog.domain.ai.port.AiClient;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Order(1)
public class GptClient implements AiClient {

  private static final Logger log = LoggerFactory.getLogger(GptClient.class);

  private final RestClient restClient;
  private final String model;
  private final ObjectMapper objectMapper;

  public GptClient(
      @Value("${app.ai.gpt.api-key}") String apiKey,
      @Value("${app.ai.gpt.api-url}") String apiUrl,
      @Value("${app.ai.gpt.model}") String model,
      ObjectMapper objectMapper) {
    this.restClient = RestClient.builder()
        .baseUrl(apiUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .build();
    this.model = model;
    this.objectMapper = objectMapper;
  }

  @Override
  public String call(String prompt) {
    try {
      Map<String, Object> body = Map.of(
          "model", model,
          "messages", List.of(
              Map.of("role", "user", "content", prompt)
          )
      );

      String response = restClient.post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(String.class);

      JsonNode root = objectMapper.readTree(response);
      return root.path("choices").path(0)
          .path("message").path("content").asText();
    } catch (Exception e) {
      log.error("GPT API 호출 실패: {}", e.getMessage());
      throw new BusinessException(ErrorCode.AI_API_CALL_FAILED);
    }
  }

  @Override
  public String getProviderName() {
    return "gpt";
  }
}
