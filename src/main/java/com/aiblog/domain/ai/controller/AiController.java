package com.aiblog.domain.ai.controller;

import com.aiblog.domain.ai.dto.AiResultResponse;
import com.aiblog.domain.ai.dto.PostRecommendationResponse;
import com.aiblog.domain.ai.entity.AiAgentType;
import com.aiblog.domain.ai.service.AiAgentService;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import com.aiblog.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}")
@RequiredArgsConstructor
public class AiController {

  private final AiAgentService aiAgentService;

  @PostMapping("/ai-feedback")
  public ResponseEntity<ApiResponse<AiResultResponse>> requestFeedback(
      @PathVariable Long postId) {
    AiResultResponse response = aiAgentService.requestFeedback(postId);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/ai-recommendation/refresh")
  public ResponseEntity<ApiResponse<AiResultResponse>> refreshRecommendation(
      @PathVariable Long postId) {
    AiResultResponse response =
        aiAgentService.refreshRecommendation(postId);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/ai-results")
  public ResponseEntity<ApiResponse<List<AiResultResponse>>> getAiResultList(
      @PathVariable Long postId,
      @RequestParam(required = false) String type) {
    List<AiResultResponse> response;
    if (type != null) {
      AiAgentType agentType;
      try {
        agentType = AiAgentType.valueOf(type.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
      }
      response = aiAgentService.getAiResultListByType(postId, agentType);
    } else {
      response = aiAgentService.getAiResultList(postId);
    }
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/recommendations")
  public ResponseEntity<ApiResponse<List<PostRecommendationResponse>>> getRecommendationList(
      @PathVariable Long postId) {
    List<PostRecommendationResponse> response =
        aiAgentService.getRecommendationList(postId);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
