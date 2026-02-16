package com.aiblog.domain.post.service;

import com.aiblog.domain.ai.repository.AiResultRepository;
import com.aiblog.domain.ai.repository.PostRecommendationRepository;
import com.aiblog.domain.attachment.repository.AttachmentRepository;
import com.aiblog.domain.category.entity.Category;
import com.aiblog.domain.category.repository.CategoryRepository;
import com.aiblog.domain.post.dto.PostCreateRequest;
import com.aiblog.domain.post.dto.PostResponse;
import com.aiblog.domain.post.dto.PostUpdateRequest;
import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.entity.PostStatus;
import com.aiblog.domain.post.repository.PostRepository;
import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final AttachmentRepository attachmentRepository;
  private final AiResultRepository aiResultRepository;
  private final PostRecommendationRepository postRecommendationRepository;

  @Transactional
  public PostResponse createPost(PostCreateRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if (postRepository.existsBySlug(request.slug())) {
      throw new BusinessException(ErrorCode.DUPLICATE_POST_SLUG);
    }

    Post post = Post.builder()
        .category(category)
        .title(request.title())
        .content(request.content())
        .slug(request.slug())
        .build();

    return PostResponse.from(postRepository.save(post));
  }

  public List<PostResponse> getPostList() {
    return postRepository
        .findByStatusWithCategory(PostStatus.PUBLISHED)
        .stream()
        .map(PostResponse::from)
        .toList();
  }

  public List<PostResponse> getPostListByCategoryId(Long categoryId) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
    }
    return postRepository
        .findByCategoryIdAndStatusWithCategory(
            categoryId, PostStatus.PUBLISHED)
        .stream()
        .map(PostResponse::from)
        .toList();
  }

  public PostResponse getPostById(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    return PostResponse.from(post);
  }

  @Transactional
  public PostResponse updatePost(Long id, PostUpdateRequest request) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if (postRepository.existsBySlugAndIdNot(request.slug(), id)) {
      throw new BusinessException(ErrorCode.DUPLICATE_POST_SLUG);
    }

    post.update(
        category,
        request.title(),
        request.content(),
        request.slug(),
        request.metaTitle(),
        request.metaDescription(),
        request.ogImage()
    );

    return PostResponse.from(post);
  }

  @Transactional
  public void deletePost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    postRecommendationRepository.deleteByPostId(id);
    postRecommendationRepository.deleteByRecommendedPostId(id);
    aiResultRepository.deleteByPostId(id);
    attachmentRepository.deleteByPostId(id);
    postRepository.delete(post);
  }

  @Transactional
  public PostResponse publishPost(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    post.publish();
    return PostResponse.from(post);
  }
}
