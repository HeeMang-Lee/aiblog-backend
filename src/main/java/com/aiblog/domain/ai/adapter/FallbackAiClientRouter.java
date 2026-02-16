package com.aiblog.domain.ai.adapter;

import com.aiblog.domain.ai.dto.AiCallResult;
import com.aiblog.domain.ai.port.AiClient;
import com.aiblog.domain.ai.port.AiClientRouter;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FallbackAiClientRouter implements AiClientRouter {

  private static final Logger log =
      LoggerFactory.getLogger(FallbackAiClientRouter.class);

  private final List<AiClient> clients;

  public FallbackAiClientRouter(List<AiClient> clients) {
    this.clients = clients;
  }

  @Override
  public AiCallResult routeAndCall(String prompt) {
    for (AiClient client : clients) {
      try {
        log.info("AI 호출 시도: {}", client.getProviderName());
        String content = client.call(prompt);
        log.info("AI 호출 성공: {}", client.getProviderName());
        return new AiCallResult(content, client.getProviderName());
      } catch (BusinessException e) {
        log.warn("AI 호출 실패, 다음 제공자로 폴백: {}",
            client.getProviderName());
      }
    }
    throw new BusinessException(ErrorCode.AI_ALL_PROVIDERS_FAILED);
  }
}
