package com.aiblog.domain.category.dto;

import com.aiblog.domain.category.entity.Category;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    int displayOrder
) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getSlug(),
        category.getDescription(),
        category.getDisplayOrder()
    );
  }
}
