package com.aiblog.domain.post.repository;

import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.entity.PostStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  List<Post> findByStatusOrderByPublishedAtDesc(PostStatus status);

  List<Post> findByCategoryIdAndStatusOrderByPublishedAtDesc(
      Long categoryId, PostStatus status);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Long id);
}
