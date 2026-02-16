package com.aiblog.domain.ai.port;

import com.aiblog.domain.ai.dto.AiCallResult;

public interface AiClientRouter {

  AiCallResult routeAndCall(String prompt);
}
