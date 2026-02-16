package com.aiblog.domain.attachment.service;

import com.aiblog.domain.attachment.dto.AttachmentCreateRequest;
import com.aiblog.domain.attachment.dto.AttachmentResponse;
import com.aiblog.domain.attachment.entity.Attachment;
import com.aiblog.domain.attachment.entity.AttachmentType;
import com.aiblog.domain.attachment.repository.AttachmentRepository;
import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.repository.PostRepository;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;
  private final PostRepository postRepository;

  @Transactional
  public AttachmentResponse createAttachment(Long postId,
      AttachmentCreateRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

    AttachmentType type = parseAttachmentType(request.type());

    Attachment attachment = Attachment.builder()
        .post(post)
        .type(type)
        .url(request.url())
        .originalFilename(request.originalFilename())
        .fileSize(request.fileSize())
        .displayOrder(request.displayOrder())
        .build();

    return AttachmentResponse.from(attachmentRepository.save(attachment));
  }

  public List<AttachmentResponse> getAttachmentListByPostId(Long postId) {
    if (!postRepository.existsById(postId)) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }
    return attachmentRepository.findByPostIdOrderByDisplayOrder(postId)
        .stream()
        .map(AttachmentResponse::from)
        .toList();
  }

  @Transactional
  public void deleteAttachment(Long id) {
    Attachment attachment = attachmentRepository.findById(id)
        .orElseThrow(
            () -> new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND));
    attachmentRepository.delete(attachment);
  }

  private AttachmentType parseAttachmentType(String type) {
    try {
      return AttachmentType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
  }
}
