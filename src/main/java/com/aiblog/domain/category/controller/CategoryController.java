package com.aiblog.domain.category.controller;

import com.aiblog.domain.category.dto.CategoryCreateRequest;
import com.aiblog.domain.category.dto.CategoryResponse;
import com.aiblog.domain.category.service.CategoryService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
      @Valid @RequestBody CategoryCreateRequest request) {
    CategoryResponse response = categoryService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryList() {
    List<CategoryResponse> response = categoryService.getCategoryList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
      @PathVariable Long id) {
    CategoryResponse response = categoryService.getCategoryById(id);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
      @PathVariable Long id,
      @Valid @RequestBody CategoryCreateRequest request) {
    CategoryResponse response = categoryService.updateCategory(id, request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCategory(
      @PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.ok(ApiResponse.ok("카테고리가 삭제되었습니다", null));
  }
}
