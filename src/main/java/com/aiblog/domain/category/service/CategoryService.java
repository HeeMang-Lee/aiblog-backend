package com.aiblog.domain.category.service;

import com.aiblog.domain.category.dto.CategoryCreateRequest;
import com.aiblog.domain.category.dto.CategoryResponse;
import com.aiblog.domain.category.entity.Category;
import com.aiblog.domain.category.repository.CategoryRepository;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public CategoryResponse createCategory(CategoryCreateRequest request) {
    if (categoryRepository.existsByName(request.name())) {
      throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
    }

    Category category = Category.builder()
        .name(request.name())
        .slug(request.slug())
        .description(request.description())
        .displayOrder(request.displayOrder())
        .build();

    return CategoryResponse.from(categoryRepository.save(category));
  }

  public List<CategoryResponse> getCategoryList() {
    return categoryRepository.findAll().stream()
        .map(CategoryResponse::from)
        .toList();
  }

  public CategoryResponse getCategoryById(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    return CategoryResponse.from(category);
  }

  @Transactional
  public CategoryResponse updateCategory(Long id,
      CategoryCreateRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
      throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
    }

    category.update(
        request.name(),
        request.slug(),
        request.description(),
        request.displayOrder()
    );

    return CategoryResponse.from(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    categoryRepository.delete(category);
  }
}
