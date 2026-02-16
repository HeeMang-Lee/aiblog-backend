package com.aiblog.domain.ai.repository;

import com.aiblog.domain.ai.entity.AiAgentType;
import com.aiblog.domain.ai.entity.AiResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResultRepository extends JpaRepository<AiResult, Long> {

  List<AiResult> findByPostIdOrderByCreatedAtDesc(Long postId);

  List<AiResult> findByPostIdAndAgentTypeOrderByCreatedAtDesc(
      Long postId, AiAgentType agentType);

  void deleteByPostId(Long postId);
}
