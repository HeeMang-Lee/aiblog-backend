package com.aiblog.domain.ai.repository;

import com.aiblog.domain.ai.entity.PostRecommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRecommendationRepository
    extends JpaRepository<PostRecommendation, Long> {

  List<PostRecommendation> findByPostIdOrderByDisplayOrder(Long postId);

  void deleteByPostId(Long postId);
}
