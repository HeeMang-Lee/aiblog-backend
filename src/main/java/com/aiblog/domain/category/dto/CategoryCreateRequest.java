package com.aiblog.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(max = 100) String slug,
    @Size(max = 255) String description,
    int displayOrder
) {
}
