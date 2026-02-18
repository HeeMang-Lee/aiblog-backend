package com.aiblog.domain.post.scheduler;

import com.aiblog.domain.post.repository.PostRepository;
import com.aiblog.domain.visitor.service.VisitorService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

  private final VisitorService visitorService;
  private final PostRepository postRepository;

  @Scheduled(fixedDelayString = "${app.visitor.sync-interval-ms}")
  @Transactional
  public void syncViewCountsToDb() {
    Set<String> postIds = visitorService.getTrackedPostIds();
    if (postIds.isEmpty()) {
      return;
    }

    int synced = 0;
    for (String postIdStr : postIds) {
      Long postId = Long.valueOf(postIdStr);
      long viewCount = visitorService.getPostViewCount(postId);
      postRepository.updateViewCount(postId, viewCount);
      synced++;
    }

    log.info("조회수 동기화 완료: {}건", synced);
  }
}
