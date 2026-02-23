package com.aiblog.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiblog.support.AcceptanceTestBase;
import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("인증 유스케이스 테스트")
class AuthAcceptanceTest extends AcceptanceTestBase {

  @Test
  @DisplayName("관리자 로그인 성공 시 JWT 토큰을 발급한다")
  void 관리자_로그인_성공_시_JWT_토큰을_발급한다() {
    // when
    var response = RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(Map.of(
            "username", "admin",
            "password", "test-password"))
        .when().post("/api/auth/login")
        .then()
        .statusCode(200)
        .extract().jsonPath();

    // then
    assertThat(response.getBoolean("success")).isTrue();
    assertThat(response.getString("data.accessToken")).isNotBlank();
  }

  @Test
  @DisplayName("잘못된 비밀번호로 로그인 시 401을 반환한다")
  void 잘못된_비밀번호로_로그인_시_401을_반환한다() {
    // when & then
    var response = RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(Map.of(
            "username", "admin",
            "password", "wrong-password"))
        .when().post("/api/auth/login")
        .then()
        .statusCode(401)
        .extract().jsonPath();

    assertThat(response.getBoolean("success")).isFalse();
  }

  @Test
  @DisplayName("토큰 없이 인증 필요 API 호출 시 401을 반환한다")
  void 토큰_없이_인증_필요_API_호출_시_401을_반환한다() {
    // when & then — POST /api/posts는 인증 필요
    RestAssured
        .given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(Map.of(
            "title", "테스트",
            "content", "내용",
            "slug", "test",
            "categoryId", 1))
        .when().post("/api/posts")
        .then()
        .statusCode(401);
  }
}
