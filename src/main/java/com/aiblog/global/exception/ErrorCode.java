package com.aiblog.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_INPUT(400, "잘못된 입력입니다"),
  UNAUTHORIZED(401, "인증이 필요합니다"),

  // Post
  POST_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),

  // Category
  CATEGORY_NOT_FOUND(404, "카테고리를 찾을 수 없습니다"),
  DUPLICATE_CATEGORY_NAME(409, "이미 존재하는 카테고리명입니다"),

  // AI
  AI_API_CALL_FAILED(502, "AI API 호출에 실패했습니다"),
  AI_ALL_PROVIDERS_FAILED(502, "모든 AI 제공자 호출에 실패했습니다"),

  // File
  FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다"),
  FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다");

  private final int status;
  private final String message;
}
