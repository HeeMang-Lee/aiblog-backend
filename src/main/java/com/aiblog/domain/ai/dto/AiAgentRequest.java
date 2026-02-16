package com.aiblog.domain.ai.dto;

public record AiAgentRequest(
    Long postId,
    String postTitle,
    String postContent,
    String contentHash
) {
}
