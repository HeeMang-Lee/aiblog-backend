package com.aiblog.domain.ai.port;

public interface AiClient {

  String call(String prompt);

  String getProviderName();
}
