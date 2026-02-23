package com.aiblog.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiblog.support.AcceptanceTestBase;
import com.aiblog.support.TestDataSetup;
import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("카테고리 유스케이스 테스트")
class CategoryAcceptanceTest extends AcceptanceTestBase {

  @BeforeEach
  void setUp() {
    TestDataSetup.execute(jdbcTemplate,
        "acceptance/category-test-data.json");
  }

  @Test
  @DisplayName("카테고리를 생성하면 목록에서 조회된다")
  void 카테고리를_생성하면_목록에서_조회된다() {
    // given
    String token = 관리자_로그인();

    // when — 카테고리 생성
    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "name", "Kotlin",
            "slug", "kotlin",
            "description", "Kotlin 관련 글",
            "displayOrder", 3))
        .when().post("/api/categories")
        .then()
        .statusCode(201);

    // then — 목록에서 조회
    var list = RestAssured
        .given()
        .when().get("/api/categories")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(list.getList("data")).hasSize(3);
    assertThat(list.getString("data[2].name")).isEqualTo("Kotlin");
  }

  @Test
  @DisplayName("중복된 이름으로 카테고리 생성 시 409를 반환한다")
  void 중복된_이름으로_카테고리_생성_시_409를_반환한다() {
    // given — "Java" 카테고리가 이미 존재 (category-test-data.json)
    String token = 관리자_로그인();

    // when & then
    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "name", "Java",
            "slug", "java-duplicate",
            "displayOrder", 99))
        .when().post("/api/categories")
        .then()
        .statusCode(409);
  }

  @Test
  @DisplayName("게시글이 있는 카테고리는 삭제할 수 없다")
  void 게시글이_있는_카테고리는_삭제할_수_없다() {
    // given — 카테고리 1에 게시글 존재 (category-test-data.json)
    String token = 관리자_로그인();

    // when & then
    RestAssured
        .given()
        .header("Authorization", "Bearer " + token)
        .when().delete("/api/categories/1")
        .then()
        .statusCode(409);
  }
}
