package com.aiblog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
    @NotNull Long categoryId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank String content,
    @NotBlank @Size(max = 255) String slug,
    @Size(max = 100) String metaTitle,
    @Size(max = 200) String metaDescription,
    @Size(max = 500) String ogImage
) {
}
