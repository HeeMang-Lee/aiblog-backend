package com.aiblog.domain.attachment.dto;

import com.aiblog.domain.attachment.entity.Attachment;
import java.time.LocalDateTime;

public record AttachmentResponse(
    Long id,
    Long postId,
    String type,
    String url,
    String originalFilename,
    Long fileSize,
    int displayOrder,
    LocalDateTime createdAt
) {

  public static AttachmentResponse from(Attachment attachment) {
    return new AttachmentResponse(
        attachment.getId(),
        attachment.getPost().getId(),
        attachment.getType().name(),
        attachment.getUrl(),
        attachment.getOriginalFilename(),
        attachment.getFileSize(),
        attachment.getDisplayOrder(),
        attachment.getCreatedAt()
    );
  }
}
