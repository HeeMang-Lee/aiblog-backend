package com.aiblog.domain.post.repository;

import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.entity.PostStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

  @Query("SELECT p FROM Post p JOIN FETCH p.category "
      + "WHERE p.status = :status ORDER BY p.publishedAt DESC")
  List<Post> findByStatusWithCategory(@Param("status") PostStatus status);

  @Query("SELECT p FROM Post p JOIN FETCH p.category "
      + "WHERE p.category.id = :categoryId AND p.status = :status "
      + "ORDER BY p.publishedAt DESC")
  List<Post> findByCategoryIdAndStatusWithCategory(
      @Param("categoryId") Long categoryId,
      @Param("status") PostStatus status);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, Long id);

  @Modifying
  @Query("DELETE FROM PostRecommendation pr "
      + "WHERE pr.post.id = :postId OR pr.recommendedPost.id = :postId")
  void deleteRecommendationsByPostId(@Param("postId") Long postId);

  @Modifying
  @Query("DELETE FROM AiResult ar WHERE ar.post.id = :postId")
  void deleteAiResultsByPostId(@Param("postId") Long postId);

  @Modifying
  @Query("DELETE FROM Attachment a WHERE a.post.id = :postId")
  void deleteAttachmentsByPostId(@Param("postId") Long postId);
}
