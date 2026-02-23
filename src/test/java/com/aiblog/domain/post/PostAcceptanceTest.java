package com.aiblog.domain.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiblog.support.AcceptanceTestBase;
import com.aiblog.support.TestDataSetup;
import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("게시글 유스케이스 테스트")
class PostAcceptanceTest extends AcceptanceTestBase {

  @BeforeEach
  void setUp() {
    TestDataSetup.execute(jdbcTemplate,
        "acceptance/post-test-data.json");
  }

  @Test
  @DisplayName("게시글을 생성하고 단건 조회할 수 있다")
  void 게시글을_생성하고_단건_조회할_수_있다() {
    // given
    String token = 관리자_로그인();

    // when — 게시글 생성
    var created = RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "title", "새 게시글",
            "content", "게시글 내용입니다",
            "slug", "new-post",
            "categoryId", 1))
        .when().post("/api/posts")
        .then()
        .statusCode(201)
        .extract().jsonPath();

    int postId = created.getInt("data.id");

    // then — 단건 조회
    var fetched = RestAssured
        .given()
        .when().get("/api/posts/" + postId)
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(fetched.getString("data.title")).isEqualTo("새 게시글");
    assertThat(fetched.getString("data.status")).isEqualTo("DRAFT");
    assertThat(fetched.getString("data.categoryName")).isEqualTo("Java");
  }

  @Test
  @DisplayName("게시글을 발행하면 목록에서 조회된다")
  void 게시글을_발행하면_목록에서_조회된다() {
    // given — 게시글 생성 (DRAFT)
    String token = 관리자_로그인();

    var created = RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "title", "발행할 게시글",
            "content", "내용",
            "slug", "publish-test",
            "categoryId", 1))
        .when().post("/api/posts")
        .then()
        .statusCode(201)
        .extract().jsonPath();

    int postId = created.getInt("data.id");

    // when — DRAFT 상태에서 목록 조회 (PUBLISHED만 나와야 함)
    var beforePublish = RestAssured
        .given()
        .when().get("/api/posts")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(beforePublish.getList("data")).isEmpty();

    // when — 발행
    RestAssured
        .given()
        .header("Authorization", "Bearer " + token)
        .when().put("/api/posts/" + postId + "/publish")
        .then()
        .statusCode(200);

    // then — 목록에서 조회됨
    var afterPublish = RestAssured
        .given()
        .when().get("/api/posts")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(afterPublish.getList("data")).hasSize(1);
    assertThat(afterPublish.getString("data[0].title"))
        .isEqualTo("발행할 게시글");
    assertThat(afterPublish.getString("data[0].status"))
        .isEqualTo("PUBLISHED");
  }

  @Test
  @DisplayName("중복된 slug로 게시글 생성 시 409를 반환한다")
  void 중복된_slug로_게시글_생성_시_409를_반환한다() {
    // given — 게시글 하나 생성
    String token = 관리자_로그인();

    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "title", "첫 번째",
            "content", "내용",
            "slug", "same-slug",
            "categoryId", 1))
        .when().post("/api/posts")
        .then()
        .statusCode(201);

    // when & then — 같은 slug로 재생성 시도
    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "title", "두 번째",
            "content", "다른 내용",
            "slug", "same-slug",
            "categoryId", 1))
        .when().post("/api/posts")
        .then()
        .statusCode(409);
  }
}
