package com.aiblog.domain.ai.repository;

import com.aiblog.domain.ai.entity.PostRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRecommendationRepository
    extends JpaRepository<PostRecommendation, Long> {

  @Query("SELECT pr FROM PostRecommendation pr "
      + "JOIN FETCH pr.post JOIN FETCH pr.recommendedPost "
      + "WHERE pr.post.id = :postId ORDER BY pr.displayOrder")
  List<PostRecommendation> findByPostIdOrderByDisplayOrder(
      @Param("postId") Long postId);

  void deleteByPostId(Long postId);
}
