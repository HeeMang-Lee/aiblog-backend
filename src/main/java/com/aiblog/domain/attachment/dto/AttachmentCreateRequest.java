package com.aiblog.domain.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttachmentCreateRequest(
    @NotNull String type,
    @NotBlank @Size(max = 1000) String url,
    @Size(max = 255) String originalFilename,
    Long fileSize,
    int displayOrder
) {
}
