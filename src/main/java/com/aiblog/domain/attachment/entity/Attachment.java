package com.aiblog.domain.attachment.entity;

import com.aiblog.domain.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AttachmentType type;

  @Column(nullable = false, length = 1000)
  private String url;

  @Column(length = 255)
  private String originalFilename;

  private Long fileSize;

  @Column(nullable = false)
  private int displayOrder;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Attachment(Post post, AttachmentType type, String url,
      String originalFilename, Long fileSize, int displayOrder) {
    this.post = post;
    this.type = type;
    this.url = url;
    this.originalFilename = originalFilename;
    this.fileSize = fileSize;
    this.displayOrder = displayOrder;
  }
}
