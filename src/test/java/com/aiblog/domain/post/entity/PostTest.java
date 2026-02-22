package com.aiblog.domain.post.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiblog.domain.category.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("게시글 엔티티 정책 테스트")
class PostTest {

  @Test
  @DisplayName("게시글 생성 시 초기 상태는 DRAFT이다")
  void 게시글_생성_시_초기_상태는_DRAFT이다() {
    // given
    Category category = Category.builder()
        .name("Java").slug("java").displayOrder(1).build();

    // when
    Post post = Post.builder()
        .category(category)
        .title("테스트 제목")
        .content("테스트 내용")
        .slug("test-slug")
        .build();

    // then
    assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
  }

  @Test
  @DisplayName("게시글 생성 시 조회수는 0이다")
  void 게시글_생성_시_조회수는_0이다() {
    // given
    Category category = Category.builder()
        .name("Java").slug("java").displayOrder(1).build();

    // when
    Post post = Post.builder()
        .category(category)
        .title("테스트 제목")
        .content("테스트 내용")
        .slug("test-slug")
        .build();

    // then
    assertThat(post.getViewCount()).isZero();
  }

  @Test
  @DisplayName("게시글 발행 시 상태가 PUBLISHED로 변경된다")
  void 게시글_발행_시_상태가_PUBLISHED로_변경된다() {
    // given
    Category category = Category.builder()
        .name("Java").slug("java").displayOrder(1).build();
    Post post = Post.builder()
        .category(category)
        .title("제목")
        .content("내용")
        .slug("slug")
        .build();

    // when
    post.publish();

    // then
    assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
  }

  @Test
  @DisplayName("게시글 발행 시 publishedAt이 설정된다")
  void 게시글_발행_시_publishedAt이_설정된다() {
    // given
    Category category = Category.builder()
        .name("Java").slug("java").displayOrder(1).build();
    Post post = Post.builder()
        .category(category)
        .title("제목")
        .content("내용")
        .slug("slug")
        .build();
    assertThat(post.getPublishedAt()).isNull();

    // when
    post.publish();

    // then
    assertThat(post.getPublishedAt()).isNotNull();
  }

  @Test
  @DisplayName("게시글 수정 시 모든 필드가 변경된다")
  void 게시글_수정_시_모든_필드가_변경된다() {
    // given
    Category original = Category.builder()
        .name("Java").slug("java").displayOrder(1).build();
    Post post = Post.builder()
        .category(original)
        .title("원본 제목")
        .content("원본 내용")
        .slug("original-slug")
        .build();

    Category updated = Category.builder()
        .name("Spring").slug("spring").displayOrder(2).build();

    // when
    post.update(updated, "수정된 제목", "수정된 내용", "updated-slug",
        "SEO 타이틀", "SEO 설명", "https://og-image.jpg");

    // then
    assertThat(post.getCategory()).isEqualTo(updated);
    assertThat(post.getTitle()).isEqualTo("수정된 제목");
    assertThat(post.getContent()).isEqualTo("수정된 내용");
    assertThat(post.getSlug()).isEqualTo("updated-slug");
    assertThat(post.getMetaTitle()).isEqualTo("SEO 타이틀");
    assertThat(post.getMetaDescription()).isEqualTo("SEO 설명");
    assertThat(post.getOgImage()).isEqualTo("https://og-image.jpg");
  }
}
