package com.aiblog.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JWT 토큰 정책 테스트")
class JwtProviderTest {

  private static final String SECRET = Base64.getEncoder()
      .encodeToString("test-secret-key-that-is-at-least-256-bits-long".getBytes());
  private static final long EXPIRATION_MS = 60_000;

  private final JwtProvider jwtProvider = new JwtProvider(SECRET, EXPIRATION_MS);

  @Test
  @DisplayName("유효한 토큰을 생성하면 검증에 성공한다")
  void 유효한_토큰을_생성하면_검증에_성공한다() {
    // given
    String token = jwtProvider.generateToken("admin");

    // when
    boolean valid = jwtProvider.validateToken(token);

    // then
    assertThat(valid).isTrue();
  }

  @Test
  @DisplayName("토큰에서 username을 추출할 수 있다")
  void 토큰에서_username을_추출할_수_있다() {
    // given
    String token = jwtProvider.generateToken("admin");

    // when
    String username = jwtProvider.getUsername(token);

    // then
    assertThat(username).isEqualTo("admin");
  }

  @Test
  @DisplayName("변조된 토큰은 검증에 실패한다")
  void 변조된_토큰은_검증에_실패한다() {
    // given
    String token = jwtProvider.generateToken("admin");
    String tampered = token.substring(0, token.length() - 5) + "xxxxx";

    // when
    boolean valid = jwtProvider.validateToken(tampered);

    // then
    assertThat(valid).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰은 검증에 실패한다")
  void 만료된_토큰은_검증에_실패한다() {
    // given — 만료 시간을 과거로 설정하여 이미 만료된 토큰 생성
    JwtProvider expiredProvider = new JwtProvider(SECRET, -1000);
    String token = expiredProvider.generateToken("admin");

    // when
    boolean valid = jwtProvider.validateToken(token);

    // then
    assertThat(valid).isFalse();
  }

  @Test
  @DisplayName("빈 문자열 토큰은 검증에 실패한다")
  void 빈_문자열_토큰은_검증에_실패한다() {
    // when
    boolean valid = jwtProvider.validateToken("");

    // then
    assertThat(valid).isFalse();
  }

  @Test
  @DisplayName("null 토큰은 검증에 실패한다")
  void null_토큰은_검증에_실패한다() {
    // when
    boolean valid = jwtProvider.validateToken(null);

    // then
    assertThat(valid).isFalse();
  }
}
