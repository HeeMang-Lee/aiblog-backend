package com.aiblog.domain.post.scheduler;

import com.aiblog.domain.post.repository.PostRepository;
import com.aiblog.domain.visitor.service.VisitorService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

  private final VisitorService visitorService;
  private final PostRepository postRepository;
  private final TransactionTemplate transactionTemplate;

  @Scheduled(fixedDelayString = "${app.visitor.sync-interval-ms}")
  public void syncViewCountsToDb() {
    Set<String> postIds = visitorService.getTrackedPostIds();
    if (postIds.isEmpty()) {
      return;
    }

    int synced = 0;
    for (String postIdStr : postIds) {
      try {
        Long postId = Long.valueOf(postIdStr);
        long viewCount = visitorService.getPostViewCount(postId);
        transactionTemplate.executeWithoutResult(status ->
            postRepository.updateViewCount(postId, viewCount));
        synced++;
      } catch (Exception e) {
        log.warn("조회수 동기화 실패: postId={}", postIdStr, e);
      }
    }

    log.info("조회수 동기화 완료: {}건", synced);
  }
}
