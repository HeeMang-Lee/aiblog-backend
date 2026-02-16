package com.aiblog.domain.attachment.controller;

import com.aiblog.domain.attachment.dto.AttachmentCreateRequest;
import com.aiblog.domain.attachment.dto.AttachmentResponse;
import com.aiblog.domain.attachment.service.AttachmentService;
import com.aiblog.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

  private final AttachmentService attachmentService;

  @PostMapping("/posts/{postId}/attachments")
  public ResponseEntity<ApiResponse<AttachmentResponse>> createAttachment(
      @PathVariable Long postId,
      @Valid @RequestBody AttachmentCreateRequest request) {
    AttachmentResponse response =
        attachmentService.createAttachment(postId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(response));
  }

  @GetMapping("/posts/{postId}/attachments")
  public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentList(
      @PathVariable Long postId) {
    List<AttachmentResponse> response =
        attachmentService.getAttachmentListByPostId(postId);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @DeleteMapping("/attachments/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAttachment(
      @PathVariable Long id) {
    attachmentService.deleteAttachment(id);
    return ResponseEntity.ok(ApiResponse.ok("첨부파일이 삭제되었습니다", null));
  }
}
