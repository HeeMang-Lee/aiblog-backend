package com.aiblog.domain.visitor.controller;

import com.aiblog.domain.visitor.dto.VisitorStatsResponse;
import com.aiblog.domain.visitor.service.VisitorService;
import com.aiblog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

  private final VisitorService visitorService;

  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<VisitorStatsResponse>> getVisitorStats() {
    VisitorStatsResponse response = visitorService.getVisitorStats();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
