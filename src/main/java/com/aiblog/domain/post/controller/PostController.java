package com.aiblog.domain.post.controller;

import com.aiblog.domain.post.dto.PostCreateRequest;
import com.aiblog.domain.post.dto.PostResponse;
import com.aiblog.domain.post.dto.PostUpdateRequest;
import com.aiblog.domain.post.service.PostService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
      @Valid @RequestBody PostCreateRequest request) {
    PostResponse response = postService.createPost(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<PostResponse>>> getPostList(
      @RequestParam(required = false) Long categoryId) {
    List<PostResponse> response = (categoryId != null)
        ? postService.getPostListByCategoryId(categoryId)
        : postService.getPostList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getPostById(
      @PathVariable Long id) {
    PostResponse response = postService.getPostById(id);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody PostUpdateRequest request) {
    PostResponse response = postService.updatePost(id, request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @PathVariable Long id) {
    postService.deletePost(id);
    return ResponseEntity.ok(ApiResponse.ok("게시글이 삭제되었습니다", null));
  }

  @PutMapping("/{id}/publish")
  public ResponseEntity<ApiResponse<PostResponse>> publishPost(
      @PathVariable Long id) {
    PostResponse response = postService.publishPost(id);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
