---
name: test
description: 테스트 코드 작성 워크플로우. 테스트 작성, 테스트 전략 수립, 테스트 리팩터링 등 모든 테스트 관련 작업 시 사용. 도메인 정책 테스트, 유스케이스(인수) 테스트, 직렬화/역직렬화 테스트를 구분하여 작성한다.
---

# Test Skill - AI Blog Backend

> 이 스킬은 "가치 있는 테스트"를 작성하기 위한 전략과 규칙을 정의한다.
> 핵심 원칙: 모든 코드에 테스트를 작성하지 않는다. 가치 있는 20%의 테스트로 80%의 신뢰성을 확보한다.

## 테스트 작성 3원칙

1. **작성 가치를 고려하여 선택적으로 작성한다**
   - 핵심 비즈니스 로직(AI 에이전트 폴백, 방문자 통계, 인증 등)은 반드시 작성
   - 단순 CRUD, 설정 클래스, 단순 위임 메서드는 작성하지 않는다
   - 판단 기준: "이 테스트가 없으면 리팩터링할 때 불안한가?"

2. **최대한 실용적으로 작성한다**
   - 하나의 통합 테스트로 여러 계층을 커버한다
   - 계층별로 각각 테스트를 작성하지 않는다
   - 테스트도 관리 대상이므로 절대적인 양을 최소화한다

3. **확실한 목적에 따라 구분하여 작성한다**
   - 도메인 정책 테스트: 비즈니스 규칙 검증
   - 유스케이스 테스트: 사용자 여정(API 호출) 검증
   - 직렬화/역직렬화 테스트: 캐시 호환성 검증

## 테스트 종류별 작성 가이드

### 1. 도메인 정책 테스트

**목적**: 도메인 엔티티 내 비즈니스 정책이 올바른지 빠르게 검증한다. 문서화 역할도 겸한다.

**작성 규칙**:
- 단위 테스트로 작성한다 (Spring 컨텍스트 불필요)
- 경계 값(boundary value)을 중심으로 테스트한다
- 모의 객체(mock)를 사용하지 않고 실제 객체를 사용한다
- 테스트 메서드명은 한글로, 정책을 그대로 서술한다

**이 프로젝트에서 도메인 정책 테스트 대상**:
- 게시글 slug 생성 규칙
- SEO 메타 태그 생성 규칙
- AI 응답 캐시 키 생성 및 해시 비교 로직
- 방문자 중복 판별 로직
- 카테고리 유효성 검증 (이름 중복 등)

**예시 패턴**:
```java
@DisplayName("AI 응답 캐시 키 생성 테스트")
class AiCacheKeyTest {

    @Test
    @DisplayName("동일한 게시글 내용이면 동일한 해시를 생성한다")
    void sameContentProducesSameHash() {
        // given
        String content = "테스트 게시글 내용";

        // when
        String hash1 = AiCacheKeyGenerator.generateContentHash(content);
        String hash2 = AiCacheKeyGenerator.generateContentHash(content);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("게시글 내용이 변경되면 해시가 달라진다")
    void differentContentProducesDifferentHash() {
        // given
        String original = "원본 내용";
        String modified = "수정된 내용";

        // when
        String hash1 = AiCacheKeyGenerator.generateContentHash(original);
        String hash2 = AiCacheKeyGenerator.generateContentHash(modified);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }
}
```

### 2. 유스케이스 테스트 (인수 테스트)

**목적**: 사용자 여정이 API 레벨에서 의도한 대로 동작하는지 검증한다. 인수 테스트이자 통합 테스트이다.

**작성 규칙**:
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` + RestAssured로 실제 HTTP 호출
- 블랙박스 테스트: API 명세 외 내부 구현을 모른다고 가정한다
- 요청/응답에 DTO 클래스를 직접 사용하지 않고 Map 또는 JSON 문자열을 사용한다
- 테스트 데이터는 JSON 파일로 관리한다 (SQL 파일 사용 금지)
- 각 테스트는 격리되어야 한다: 테스트 전 INSERT, 테스트 후 TRUNCATE
- 테스트 메서드명은 한글로, 사용자 시나리오를 서술한다

**이 프로젝트에서 유스케이스 테스트 대상**:
- 게시글 CRUD 전체 흐름
- 카테고리별 게시글 목록 조회
- AI 피드백 요청 → 응답 (폴백 포함)
- AI 추천 생성 → 캐시 적중 → 내용 변경 후 캐시 무효화
- 인증 흐름: 로그인 → JWT 발급 → 인증 필요 API 호출
- 파일 업로드 → 게시글에 첨부
- 방문자 통계 증가 → 조회

**예시 패턴**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfile("test")
class PostAcceptanceTest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        // JSON 파일에서 테스트 데이터 INSERT
        TestDataSetup.execute(jdbcTemplate, "acceptance/post-test-data.json");
    }

    @AfterEach
    void tearDown() {
        // 모든 사용자 테이블 TRUNCATE
        TestDataSetup.truncateAll(jdbcTemplate);
    }

    @Test
    @DisplayName("게시글을 생성하면 카테고리별 목록에서 조회된다")
    void createPostAndFindByCategory() {
        // given - 관리자 로그인
        String token = 관리자_로그인();

        // when - 게시글 생성
        var 생성결과 = RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + token)
            .body(Map.of(
                "title", "테스트 게시글",
                "content", "내용입니다",
                "categoryId", 1
            ))
            .when().post("/api/posts")
            .then().log().all()
            .statusCode(200)
            .extract().jsonPath();

        // then - 카테고리별 목록에서 조회
        var 목록 = RestAssured
            .given().log().all()
            .when().get("/api/categories/1/posts")
            .then().log().all()
            .statusCode(200)
            .extract().jsonPath();

        assertThat(목록.getList("data")).hasSize(1);
        assertThat(목록.getString("data[0].title")).isEqualTo("테스트 게시글");
    }
}
```

**테스트 데이터 JSON 파일 형식** (`src/test/resources/acceptance/`):
```json
{
  "category": [
    {
      "id": 1,
      "name": "Java",
      "slug": "java",
      "created_at": "2025-01-01 00:00:00",
      "updated_at": "2025-01-01 00:00:00"
    }
  ]
}
```

### 3. 직렬화/역직렬화 테스트 (승인 테스트)

**목적**: Redis 캐시에 저장되는 객체의 직렬화/역직렬화 호환성을 검증한다. 필드 변경 시 캐시 버저닝 필요성을 알려주는 신호 역할.

**작성 규칙**:
- 단위 테스트로 작성한다
- 스냅샷(Golden) 방식: 직렬화 결과를 저장해두고, 이후 결과와 비교한다
- 이 테스트가 실패하면 → 캐시 버저닝이 필요하다는 신호

**이 프로젝트에서 직렬화/역직렬화 테스트 대상**:
- AI 응답 캐시 객체 (AiAgentResponse)
- 방문자 통계 관련 캐시 객체

**예시 패턴**:
```java
class AiResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AI 응답 객체의 직렬화 결과가 기존 스냅샷과 동일하다")
    void serializationCompatibility() throws Exception {
        // given
        AiAgentResponse response = AiAgentResponse.builder()
            .agentType(AiAgentType.FEEDBACK)
            .content("피드백 내용")
            .postId(1L)
            .build();

        // when
        String json = objectMapper.writeValueAsString(response);

        // then - 스냅샷 파일과 비교
        String snapshot = Files.readString(
            Path.of("src/test/resources/snapshots/ai-response.json")
        );
        assertThat(json).isEqualTo(snapshot);
    }

    @Test
    @DisplayName("기존 스냅샷 JSON을 현재 객체로 역직렬화할 수 있다")
    void deserializationCompatibility() throws Exception {
        // given
        String snapshot = Files.readString(
            Path.of("src/test/resources/snapshots/ai-response.json")
        );

        // when & then - 역직렬화 실패 시 캐시 버저닝 필요
        assertThatCode(() ->
            objectMapper.readValue(snapshot, AiAgentResponse.class)
        ).doesNotThrowAnyException();
    }
}
```

## 테스트 대역(Test Double) 사용 기준

모의 객체 프레임워크 사용을 최소화하고, 가능한 한 실제 객체를 사용한다.

### 실제 객체 사용 (기본)
- 도메인 엔티티 간 협력
- Repository (H2 인메모리 DB 사용)
- Service 간 호출

### Fake 객체 사용: 외부 서비스
- **AI API 클라이언트 (GPT, Claude)**: 실제 API 호출 불가, 비용 발생
  ```java
  @Profile("test")
  @Component
  public class FakeGptClient implements AiClient {
      @Override
      public String call(String prompt) {
          return "테스트용 AI 응답: " + prompt.substring(0, Math.min(20, prompt.length()));
      }

      @Override
      public String getProviderName() {
          return "fake-gpt";
      }
  }
  ```
- **파일 저장소**: 실제 파일 시스템 대신 인메모리 또는 임시 디렉토리 사용

### Dummy 객체 사용: 부수효과가 있는 기능
- 알림 발송, 외부 웹훅 호출 등 테스트 시 실행되면 안 되는 기능
  ```java
  @Profile("test")
  @Component
  public class DummyNotificationService implements NotificationService {
      @Override
      public void send(String message) {
          // Do nothing
      }
  }
  ```

### Mock 사용 (최후의 수단): 데이터 확보가 어려운 경우
- 시간에 의존하는 로직 (가입 N일 이내 등)
- 복잡한 외부 상태에 의존하는 경우
- **주의**: 테스트 컨텍스트 캐싱이 깨져 속도가 느려진다

## 테스트 코드 스타일 규칙

### 네이밍
- 테스트 메서드명은 **한글**로 작성한다 (비즈니스 문서 역할)
- `@DisplayName`을 반드시 사용한다
- 도메인 정책 테스트: 정책을 그대로 서술 → `"프로모션 참여는 18세 이하만 가능하다"`
- 유스케이스 테스트: 사용자 시나리오 서술 → `"게시글을 생성하면 카테고리별 목록에서 조회된다"`

### 구조: given-when-then
```java
@Test
@DisplayName("설명")
void testName() {
    // given - 테스트 준비 (최소한으로)

    // when - 테스트 실행 (한 줄)

    // then - 결과 검증
}
```

### 완전한 테스트 (Complete Test)
- 읽는 사람이 결과에 도달하기까지 필요한 모든 정보를 테스트 본문에 담는다
- 테스트 데이터를 외부 파일(JSON)에 정의하되, 어떤 파일을 사용하는지 명시한다
- 헬퍼 메서드에 과도하게 숨기지 않는다

### 간결한 테스트 (Concise Test)
- 테스트와 무관한 설정 코드는 `@BeforeEach`로 빼낸다
- 한 테스트에서 한 가지만 검증한다
- 불필요한 필드 세팅은 하지 않는다 (검증 대상과 관련된 것만)

## 테스트 디렉토리 구조

```
src/test/
├── java/com/aiblog/
│   ├── domain/
│   │   ├── post/
│   │   │   ├── PostAcceptanceTest.java          # 유스케이스(인수) 테스트
│   │   │   └── entity/
│   │   │       └── PostSlugTest.java            # 도메인 정책 테스트
│   │   ├── category/
│   │   │   ├── CategoryAcceptanceTest.java
│   │   │   └── entity/
│   │   │       └── CategoryValidationTest.java
│   │   ├── ai/
│   │   │   ├── AiFeedbackAcceptanceTest.java
│   │   │   ├── AiRecommendAcceptanceTest.java
│   │   │   ├── cache/
│   │   │   │   ├── AiCacheKeyTest.java          # 도메인 정책 테스트
│   │   │   │   └── AiResponseSerializationTest.java  # 직렬화 테스트
│   │   │   └── client/
│   │   │       └── AiClientRouterTest.java      # 폴백 전략 테스트
│   │   └── visitor/
│   │       ├── VisitorAcceptanceTest.java
│   │       └── VisitorCountTest.java
│   ├── global/
│   │   └── security/
│   │       └── AuthAcceptanceTest.java
│   └── support/
│       ├── AcceptanceTestBase.java              # 공통 설정 추상 클래스
│       └── TestDataSetup.java                   # JSON → DB INSERT 유틸
└── resources/
    ├── acceptance/                              # 유스케이스 테스트 데이터
    │   ├── post-test-data.json
    │   ├── category-test-data.json
    │   └── ai-test-data.json
    ├── snapshots/                               # 직렬화 스냅샷
    │   └── ai-response.json
    └── application-test.yml                     # 테스트 프로필 설정
```

## 작성하지 않는 테스트

다음은 이 프로젝트에서 테스트를 작성하지 않는 대상이다:

- 단순 getter/setter, record의 접근자
- 설정 클래스 (`@Configuration`, `SecurityConfig` 등)
- DTO의 단순 변환 (`PostResponse.from(post)`)
- 단순 위임만 하는 Controller (유스케이스 테스트에서 커버)
- 단순 CRUD만 하는 Repository (유스케이스 테스트에서 커버)
- SEO 관련 정적 파일 제공 (robots.txt, sitemap.xml의 존재 여부 정도만)

## FIRST 규칙 체크리스트

테스트 작성 후 아래를 확인한다:

- [ ] **Fast**: 단위 테스트는 1초 이내, 통합 테스트는 10초 이내에 실행되는가?
- [ ] **Independent**: 테스트 간 실행 순서에 의존하지 않는가? 데이터가 격리되었는가?
- [ ] **Repeatable**: 여러 번 실행해도 동일한 결과가 나오는가? (시간 의존성, 랜덤 의존성 없는가?)
- [ ] **Self-Validating**: 성공/실패가 자동으로 판단되는가? (수동 확인 불필요한가?)
- [ ] **Timely**: 구현 코드와 함께 작성되었는가?

## 테스트 기술 스택

- **JUnit 5**: 테스트 프레임워크
- **AssertJ**: 가독성 높은 assertion
- **RestAssured**: HTTP 호출 기반 인수 테스트
- **H2**: 인메모리 테스트 데이터베이스
- **Embedded Redis** (또는 Testcontainers): Redis 테스트 환경
- **@ActiveProfiles("test")**: 테스트 프로필로 Fake/Dummy 빈 활성화

## 절대 하지 말 것

- 커버리지 수치를 올리기 위한 의미 없는 테스트 작성
- 모든 계층에 대한 개별 단위 테스트 작성 (통합 테스트로 커버)
- `@MockBean`, `@SpyBean`의 과도한 사용 (테스트 컨텍스트 캐싱이 깨진다)
- 테스트에서 프로덕션 DTO 클래스를 직접 import하여 요청 생성 (블랙박스 위반)
- `Thread.sleep()`으로 비동기 테스트 처리
- 한 테스트 메서드에서 여러 시나리오를 검증
- 테스트 간 공유 상태 사용 (static 변수 등)
- 테스트 실패를 무시하고 `@Disabled` 처리
