package com.aiblog.domain.post.entity;

import com.aiblog.domain.category.entity.Category;
import com.aiblog.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PostStatus status;

  @Column(length = 100)
  private String metaTitle;

  @Column(length = 200)
  private String metaDescription;

  @Column(length = 500)
  private String ogImage;

  @Column(nullable = false)
  private long viewCount;

  private LocalDateTime publishedAt;

  @Builder
  public Post(Category category, String title, String content, String slug) {
    this.category = category;
    this.title = title;
    this.content = content;
    this.slug = slug;
    this.status = PostStatus.DRAFT;
    this.viewCount = 0;
  }

  public void update(Category category, String title, String content,
      String slug, String metaTitle, String metaDescription,
      String ogImage) {
    this.category = category;
    this.title = title;
    this.content = content;
    this.slug = slug;
    this.metaTitle = metaTitle;
    this.metaDescription = metaDescription;
    this.ogImage = ogImage;
  }

  public void publish() {
    this.status = PostStatus.PUBLISHED;
    this.publishedAt = LocalDateTime.now();
  }
}
