package com.aiblog.domain.ai.service;

import com.aiblog.domain.ai.cache.AiCacheService;
import com.aiblog.domain.ai.dto.AiAgentRequest;
import com.aiblog.domain.ai.dto.AiAgentResponse;
import com.aiblog.domain.ai.dto.AiResultResponse;
import com.aiblog.domain.ai.dto.PostRecommendationResponse;
import com.aiblog.domain.ai.entity.AiAgentType;
import com.aiblog.domain.ai.entity.AiResult;
import com.aiblog.domain.ai.port.AiAgent;
import com.aiblog.domain.ai.repository.AiResultRepository;
import com.aiblog.domain.ai.repository.PostRecommendationRepository;
import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.repository.PostRepository;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jakarta.annotation.PostConstruct;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiAgentService {

  private final List<AiAgent> agents;
  private final AiResultRepository aiResultRepository;
  private final PostRecommendationRepository postRecommendationRepository;
  private final PostRepository postRepository;
  private final AiCacheService aiCacheService;

  private Map<AiAgentType, AiAgent> agentMap;

  @PostConstruct
  void init() {
    agentMap = agents.stream()
        .collect(Collectors.toMap(AiAgent::getType, Function.identity()));
  }

  @Transactional
  public AiResultResponse requestFeedback(Long postId) {
    return executeAgent(postId, AiAgentType.FEEDBACK);
  }

  @Transactional
  public AiResultResponse refreshRecommendation(Long postId) {
    return executeAgent(postId, AiAgentType.RECOMMENDATION);
  }

  public List<AiResultResponse> getAiResultList(Long postId) {
    validatePostExists(postId);
    return aiResultRepository.findByPostIdOrderByCreatedAtDesc(postId)
        .stream()
        .map(AiResultResponse::from)
        .toList();
  }

  public List<AiResultResponse> getAiResultListByType(Long postId,
      AiAgentType agentType) {
    validatePostExists(postId);
    return aiResultRepository
        .findByPostIdAndAgentTypeOrderByCreatedAtDesc(postId, agentType)
        .stream()
        .map(AiResultResponse::from)
        .toList();
  }

  public List<PostRecommendationResponse> getRecommendationList(
      Long postId) {
    validatePostExists(postId);
    return postRecommendationRepository
        .findByPostIdOrderByDisplayOrder(postId)
        .stream()
        .map(PostRecommendationResponse::from)
        .toList();
  }

  private AiResultResponse executeAgent(Long postId,
      AiAgentType agentType) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

    String contentHash = sha256(post.getContent());

    Optional<String> cached =
        aiCacheService.get(agentType, postId, contentHash);
    if (cached.isPresent()) {
      return findLatestResult(postId, agentType);
    }

    AiAgent agent = findAgent(agentType);
    AiAgentRequest request = new AiAgentRequest(
        postId, post.getTitle(), post.getContent(), contentHash);
    AiAgentResponse response = agent.execute(request);

    aiCacheService.set(agentType, postId, contentHash, response.content());

    AiResult aiResult = AiResult.builder()
        .post(post)
        .agentType(agentType)
        .content(response.content())
        .contentHash(contentHash)
        .provider(response.provider())
        .build();
    aiResultRepository.save(aiResult);

    return AiResultResponse.from(aiResult);
  }

  private AiAgent findAgent(AiAgentType type) {
    AiAgent agent = agentMap.get(type);
    if (agent == null) {
      throw new IllegalStateException(
          "등록된 에이전트를 찾을 수 없습니다: " + type);
    }
    return agent;
  }

  private AiResultResponse findLatestResult(Long postId,
      AiAgentType agentType) {
    return aiResultRepository
        .findByPostIdAndAgentTypeOrderByCreatedAtDesc(postId, agentType)
        .stream()
        .findFirst()
        .map(AiResultResponse::from)
        .orElseThrow(
            () -> new BusinessException(ErrorCode.AI_RESULT_NOT_FOUND));
  }

  private void validatePostExists(Long postId) {
    if (!postRepository.existsById(postId)) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(
          input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다");
    }
  }
}
