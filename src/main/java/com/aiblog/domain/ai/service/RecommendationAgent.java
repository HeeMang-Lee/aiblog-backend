package com.aiblog.domain.ai.service;

import com.aiblog.domain.ai.dto.AiAgentRequest;
import com.aiblog.domain.ai.dto.AiAgentResponse;
import com.aiblog.domain.ai.dto.AiCallResult;
import com.aiblog.domain.ai.entity.AiAgentType;
import com.aiblog.domain.ai.port.AiAgent;
import com.aiblog.domain.ai.port.AiClientRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationAgent implements AiAgent {

  private final AiClientRouter aiClientRouter;

  @Override
  public AiAgentResponse execute(AiAgentRequest request) {
    String prompt = buildPrompt(request);
    AiCallResult result = aiClientRouter.routeAndCall(prompt);
    return new AiAgentResponse(result.content(), result.provider());
  }

  @Override
  public AiAgentType getType() {
    return AiAgentType.RECOMMENDATION;
  }

  private String buildPrompt(AiAgentRequest request) {
    return """
        당신은 블로그 글 추천 전문가입니다.
        아래 블로그 글과 관련된 추천 주제를 제안해주세요.
        JSON 배열로 응답해주세요.

        형식:
        [{"title": "추천 글 제목", "reason": "추천 사유"}]

        제목: %s

        본문:
        %s
        """.formatted(request.postTitle(), request.postContent());
  }
}
