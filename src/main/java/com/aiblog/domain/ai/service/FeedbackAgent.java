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
public class FeedbackAgent implements AiAgent {

  private final AiClientRouter aiClientRouter;

  @Override
  public AiAgentResponse execute(AiAgentRequest request) {
    String prompt = buildPrompt(request);
    AiCallResult result = aiClientRouter.routeAndCall(prompt);
    return new AiAgentResponse(result.content(), result.provider());
  }

  @Override
  public AiAgentType getType() {
    return AiAgentType.FEEDBACK;
  }

  private String buildPrompt(AiAgentRequest request) {
    return """
        당신은 블로그 글 피드백 전문가입니다.
        아래 블로그 글을 읽고 다음 관점에서 피드백을 제공해주세요:
        1. 구조와 흐름
        2. 기술적 정확성
        3. 가독성과 문체
        4. 개선 제안

        제목: %s

        본문:
        %s
        """.formatted(request.postTitle(), request.postContent());
  }
}
