package com.aiblog.domain.attachment.repository;

import com.aiblog.domain.attachment.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

  List<Attachment> findByPostIdOrderByDisplayOrder(Long postId);

  void deleteByPostId(Long postId);
}
