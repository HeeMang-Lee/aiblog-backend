package com.aiblog.domain.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiblog.support.AcceptanceTestBase;
import com.aiblog.support.TestDataSetup;
import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("첨부파일 유스케이스 테스트")
class AttachmentAcceptanceTest extends AcceptanceTestBase {

  @BeforeEach
  void setUp() {
    TestDataSetup.execute(jdbcTemplate,
        "acceptance/attachment-test-data.json");
  }

  @Test
  @DisplayName("게시글에 첨부파일을 추가하고 삭제할 수 있다")
  void 게시글에_첨부파일을_추가하고_삭제할_수_있다() {
    // given
    String token = 관리자_로그인();

    // when — 첨부파일 추가
    var created = RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "type", "IMAGE",
            "url", "/uploads/images/test.jpg",
            "originalFilename", "test.jpg",
            "fileSize", 1024,
            "displayOrder", 1))
        .when().post("/api/posts/1/attachments")
        .then()
        .statusCode(201)
        .extract().jsonPath();

    int attachmentId = created.getInt("data.id");

    // then — 목록 조회 (공개 API)
    var list = RestAssured
        .given()
        .when().get("/api/posts/1/attachments")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(list.getList("data")).hasSize(1);
    assertThat(list.getString("data[0].originalFilename"))
        .isEqualTo("test.jpg");

    // when — 삭제
    RestAssured
        .given()
        .header("Authorization", "Bearer " + token)
        .when().delete("/api/attachments/" + attachmentId)
        .then()
        .statusCode(200);

    // then — 삭제 후 목록 비어있음
    var afterDelete = RestAssured
        .given()
        .when().get("/api/posts/1/attachments")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    assertThat(afterDelete.getList("data")).isEmpty();
  }

  @Test
  @DisplayName("존재하지 않는 게시글에 첨부파일 추가 시 404를 반환한다")
  void 존재하지_않는_게시글에_첨부파일_추가_시_404를_반환한다() {
    // given
    String token = 관리자_로그인();

    // when & then
    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .header("Authorization", "Bearer " + token)
        .body(Map.of(
            "type", "IMAGE",
            "url", "/uploads/images/test.jpg",
            "originalFilename", "test.jpg",
            "fileSize", 1024,
            "displayOrder", 1))
        .when().post("/api/posts/999/attachments")
        .then()
        .statusCode(404);
  }
}
