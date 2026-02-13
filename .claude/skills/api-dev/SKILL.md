---
name: api-dev
description: AI 블로그 백엔드 API 개발 워크플로우. API 구현, 엔티티 설계, 예외 처리 등 백엔드 개발 작업 시 사용. 실용적 헥사고날 아키텍처를 따른다.
---

# AI Blog Backend - API Development Skill

> 이 스킬은 AI Agent 기반 개인 블로그 플랫폼 백엔드 개발 시 따라야 할 워크플로우와 규칙을 정의합니다.
> 아키텍처: **실용적 헥사고날** — 복잡도가 높은 도메인(ai, storage)에만 포트/어댑터를 적용하고, 나머지는 레이어드 기반으로 심플하게 유지합니다.

## 기술 스택

- Java 21 / Spring Boot 4.0.x / Gradle
- MySQL (메인 DB) / Redis (캐싱, 방문자 통계)
- AI API: GPT (기본) → Claude (폴백), 순서 변경 가능
- 파일 저장: 로컬 (FileStorageService 인터페이스로 추상화)
- 인증: Spring Security + JWT + application.yml 고정 관리자 계정

## 아키텍처: 실용적 헥사고날

### 설계 원칙

이 프로젝트는 테이블 5개, 개발자 1명의 개인 블로그이다.
헥사고날 아키텍처를 풀로 적용하면 보일러플레이트가 비즈니스 코드보다 많아지는 오버엔지니어링이 된다.
따라서 **교체 가능성이 실제로 있는 외부 의존성에만** 포트/어댑터를 적용하고, 나머지는 레이어드로 유지한다.

### 포트/어댑터를 적용하는 기준

| 대상 | 인터페이스(포트) | 이유 |
|------|-----------------|------|
| AI Client | `AiClient` (포트) | GPT ↔ Claude 교체/추가 확실 |
| AI Client 라우팅 | `AiClientRouter` (포트) | 폴백 전략 교체 가능 |
| AI Agent | `AiAgent` (포트) | 에이전트 종류 추가 확실 |
| 파일 저장소 | `FileStorageService` (포트) | 로컬 → S3/R2 전환 계획 있음 |
| PostService | **인터페이스 없음** | 구현체가 하나, 교체 이유 없음 |
| CategoryService | **인터페이스 없음** | 구현체가 하나, 교체 이유 없음 |
| Repository | **인터페이스 없음** | Spring Data JPA가 이미 인터페이스 |

**핵심 원칙: 구현체가 하나면 인터페이스를 만들지 않는다.**

### 패키지 구조

```
com.aiblog
│
├── domain/
│   │
│   ├── post/                          # ── 레이어드 (심플) ──
│   │   ├── controller/
│   │   │   └── PostController.java
│   │   ├── service/
│   │   │   └── PostService.java              ← 인터페이스 없이 구체 클래스
│   │   ├── repository/
│   │   │   └── PostRepository.java
│   │   ├── entity/
│   │   │   └── Post.java
│   │   └── dto/
│   │       ├── PostCreateRequest.java
│   │       ├── PostUpdateRequest.java
│   │       └── PostResponse.java
│   │
│   ├── category/                      # ── 레이어드 (심플) ──
│   │   ├── controller/
│   │   │   └── CategoryController.java
│   │   ├── service/
│   │   │   └── CategoryService.java
│   │   ├── repository/
│   │   │   └── CategoryRepository.java
│   │   ├── entity/
│   │   │   └── Category.java
│   │   └── dto/
│   │
│   ├── attachment/                    # ── 레이어드 (심플) ──
│   │   ├── controller/
│   │   │   └── AttachmentController.java
│   │   ├── service/
│   │   │   └── AttachmentService.java
│   │   ├── repository/
│   │   │   └── AttachmentRepository.java
│   │   ├── entity/
│   │   │   └── Attachment.java
│   │   └── dto/
│   │
│   ├── ai/                            # ── 헥사고날 (포트/어댑터) ──
│   │   ├── controller/
│   │   │   └── AiController.java             ← Driving Adapter (진입점)
│   │   ├── service/
│   │   │   ├── AiAgentService.java           ← Application Core (오케스트레이션)
│   │   │   ├── FeedbackAgent.java            ← AiAgent 구현체
│   │   │   └── RecommendationAgent.java      ← AiAgent 구현체
│   │   ├── port/
│   │   │   ├── AiClient.java                 ← Port (외부 AI API 추상화)
│   │   │   ├── AiClientRouter.java           ← Port (폴백 전략 추상화)
│   │   │   └── AiAgent.java                  ← Port (에이전트 추상화)
│   │   ├── adapter/
│   │   │   ├── GptClient.java                ← Adapter (GPT 구현체)
│   │   │   ├── ClaudeClient.java             ← Adapter (Claude 구현체)
│   │   │   └── FallbackAiClientRouter.java   ← Adapter (순차 폴백 구현체)
│   │   ├── cache/
│   │   │   └── AiCacheService.java           ← Redis 캐싱 전략
│   │   ├── repository/
│   │   │   ├── AiResultRepository.java
│   │   │   └── PostRecommendationRepository.java
│   │   ├── entity/
│   │   │   ├── AiResult.java
│   │   │   └── PostRecommendation.java
│   │   └── dto/
│   │
│   ├── visitor/                       # ── 레이어드 (심플) ──
│   │   ├── controller/
│   │   │   └── VisitorController.java
│   │   ├── service/
│   │   │   └── VisitorService.java
│   │   └── dto/
│   │
│   └── seo/                           # ── 레이어드 (심플) ──
│       ├── controller/
│       │   └── SeoController.java
│       └── service/
│           └── SitemapService.java
│
├── global/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── WebConfig.java
│   │   └── AsyncConfig.java
│   ├── security/
│   │   ├── JwtProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── AuthController.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   ├── response/
│   │   └── ApiResponse.java
│   ├── storage/                       # ── 헥사고날 (포트/어댑터) ──
│   │   ├── FileStorageService.java           ← Port
│   │   └── LocalFileStorageService.java      ← Adapter (현재 구현체)
│   └── common/
│       └── BaseEntity.java
│
└── BlogApplication.java
```

### 의존성 흐름

```
[레이어드 도메인 - post, category, attachment, visitor, seo]

    Controller → Service → Repository
                    ↓
                  Entity

[헥사고날 도메인 - ai]

    Controller → AiAgentService → Port(AiClient)     → Adapter(GptClient)
    (Driving      (Application     (인터페이스)          (Driven Adapter)
     Adapter)       Core)                               Adapter(ClaudeClient)
                      ↓
                 Port(AiAgent) → Adapter(FeedbackAgent)
                                 Adapter(RecommendationAgent)

[헥사고날 인프라 - storage]

    Service → Port(FileStorageService) → Adapter(LocalFileStorageService)
                                         추후: Adapter(S3FileStorageService)
```

### 도메인 간 의존 규칙

```
post ← attachment (Attachment가 Post를 참조)
post ← ai        (AiResult가 Post를 참조)
post ← visitor   (VisitorService가 postId를 받음)
post ← seo       (SitemapService가 Post 목록을 조회)
category ← post  (Post가 Category를 참조)
```

- 같은 레벨의 도메인 간 Service 주입은 허용한다 (예: PostService에서 CategoryService 주입)
- 단, 순환 의존은 금지한다
- AI 도메인은 Post 도메인에 의존하지만 (게시글 내용이 필요), Post 도메인은 AI 도메인에 의존하지 않는다
- 게시글 발행 시 AI 추천 실행은 PostService에서 AiAgentService를 직접 호출한다 (현재 규모에서 이벤트 기반은 과잉)

## 워크플로우

모든 API 개발은 아래 순서를 반드시 따른다:

### Step 1: 요구사항 확인
- 구현할 기능의 범위를 사용자에게 확인받는다
- 기존 코드와 충돌 여부를 먼저 파악한다
- 불명확한 부분은 구현 전에 반드시 질문한다

### Step 2: 설계
- 어떤 패키지에 어떤 클래스를 만들지 목록을 먼저 제시한다
- 포트/어댑터 적용 대상인지 확인한다 (AI, Storage만 해당)
- DB 스키마 변경이 필요하면 엔티티 변경사항을 먼저 보여준다
- 사용자 승인 후 구현에 들어간다

### Step 3: 구현 순서
1. Entity / DTO 정의
2. Port 인터페이스 정의 (해당하는 경우만)
3. Repository 인터페이스
4. Service / Adapter 비즈니스 로직
5. Controller (REST API)
6. 예외 처리 연결

### Step 4: 검증
- 컴파일 에러 없는지 확인
- 기존 코드와의 의존성 확인
- 도메인 간 순환 의존 없는지 확인
- API 요청/응답 예시 제공

## 레이어 규칙

### Controller
- HTTP 요청/응답만 담당한다
- 비즈니스 로직을 절대 넣지 않는다
- `@RestController` + `@RequestMapping` 사용
- 메서드별 `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Request DTO로 받고, Response DTO로 응답한다

### Service
- `@Service` + `@Transactional` 사용
- 레이어드 도메인(post, category 등): 인터페이스 없이 구체 클래스
- 헥사고날 도메인(ai): Port 인터페이스를 통해 외부 의존성에 접근
- 다른 Service를 주입받을 수 있지만, 순환 의존은 금지

### Port (헥사고날 도메인만)
- 순수 자바 인터페이스, 프레임워크 어노테이션 없음
- 외부 의존성의 계약만 정의
- Service가 Port에 의존하고, Adapter가 Port를 구현

### Adapter (헥사고날 도메인만)
- Port 인터페이스의 구현체
- `@Component`로 Spring 빈 등록
- 외부 라이브러리/API에 대한 실제 구현 담당

### Repository
- Spring Data JPA 사용
- 커스텀 쿼리는 `@Query` 또는 QueryDSL
- 네이밍: `findByXxx`, `existsByXxx` 등 Spring Data 컨벤션

### Entity
- `@Entity` + `@Table(name = "...")` 명시
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@Column` 어노테이션으로 nullable, length 등 명시
- BaseEntity로 createdAt, updatedAt 공통 관리
- Lombok: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@Builder`
- `@Setter` 사용 금지 → 의미 있는 메서드명으로 상태 변경
- 연관관계: `@ManyToOne(fetch = FetchType.LAZY)` 단방향만 허용
- `@OneToMany` (양방향) 금지
- `cascade` 전부 금지
- DB FK 제약조건은 유지 (이 프로젝트는 무결성 우선)

## 네이밍 컨벤션

### 클래스 네이밍

**레이어드 도메인:**
- Controller: `PostController`, `CategoryController`
- Service: `PostService`, `CategoryService`
- Repository: `PostRepository`
- Entity: `Post`, `Category`
- Request DTO: `PostCreateRequest`, `PostUpdateRequest`
- Response DTO: `PostResponse`, `PostListResponse`
- Exception: `PostNotFoundException`

**헥사고날 도메인 (AI):**
- Port: `AiClient`, `AiClientRouter`, `AiAgent`
- Adapter: `GptClient`, `ClaudeClient`, `FallbackAiClientRouter`
- Service: `AiAgentService` (오케스트레이션)
- Agent 구현체: `FeedbackAgent`, `RecommendationAgent`

**헥사고날 인프라 (Storage):**
- Port: `FileStorageService`
- Adapter: `LocalFileStorageService`, 추후 `S3FileStorageService`

### 메서드 네이밍
- 생성: `createXxx`
- 조회(단건): `getXxx`, `getXxxById`
- 조회(목록): `getXxxList`, `getXxxListByCategoryId`
- 수정: `updateXxx`
- 삭제: `deleteXxx`

### API 경로
- REST 컨벤션: 복수형 명사, kebab-case
- 예시: `/api/posts`, `/api/categories`, `/api/posts/{postId}/ai-feedback`

## AI 에이전트 설계 규칙

### Port 정의
```java
// AI API 호출 추상화
public interface AiClient {
    String call(String prompt);
    String getProviderName();
}

// 폴백 전략 추상화
public interface AiClientRouter {
    String routeAndCall(String prompt);
}

// 에이전트 추상화
public interface AiAgent {
    AiAgentResponse execute(AiAgentRequest request);
    AiAgentType getType();
}
```

### Adapter 구현
```java
// GPT Adapter
@Component
public class GptClient implements AiClient {
    @Override
    public String call(String prompt) { /* GPT API 호출 */ }
    @Override
    public String getProviderName() { return "gpt"; }
}

// Claude Adapter
@Component
public class ClaudeClient implements AiClient {
    @Override
    public String call(String prompt) { /* Claude API 호출 */ }
    @Override
    public String getProviderName() { return "claude"; }
}

// 순차 폴백 Adapter
@Component
public class FallbackAiClientRouter implements AiClientRouter {
    private final List<AiClient> clients; // 설정된 순서대로 주입
    @Override
    public String routeAndCall(String prompt) {
        // 순서대로 시도, 실패 시 다음 클라이언트로 폴백
    }
}
```

### AI 응답 캐싱
- 캐시 키: `ai:{agentType}:{postId}:{contentHash}`
- contentHash: 게시글 내용의 SHA-256 해시
- 내용 변경(해시 변경) 시 자동 캐시 무효화
- TTL 기반 만료 적용

## 응답 형식

모든 API는 공통 응답 형식을 사용한다:

```java
public record ApiResponse<T>(
    boolean success,
    String message,
    T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

## 예외 처리

### 커스텀 예외 구조
```java
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
}

public enum ErrorCode {
    // Common
    INVALID_INPUT(400, "잘못된 입력입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),

    // Post
    POST_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),

    // Category
    CATEGORY_NOT_FOUND(404, "카테고리를 찾을 수 없습니다"),
    DUPLICATE_CATEGORY_NAME(409, "이미 존재하는 카테고리명입니다"),

    // AI
    AI_API_CALL_FAILED(502, "AI API 호출에 실패했습니다"),
    AI_ALL_PROVIDERS_FAILED(502, "모든 AI 제공자 호출에 실패했습니다"),

    // File
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다"),
    FILE_NOT_FOUND(404, "파일을 찾을 수 없습니다");
}
```

### GlobalExceptionHandler
- `@RestControllerAdvice`로 전역 처리
- BusinessException → ErrorCode 기반 응답
- MethodArgumentNotValidException → 유효성 검증 에러
- 그 외 Exception → 500 Internal Server Error

## Redis 사용 규칙

### 키 네이밍
- 방문자 통계: `visitor:total`, `visitor:today:{yyyyMMdd}`, `visitor:post:{postId}`
- 방문자 추적 대상: `visitor:tracked-posts` (동기화 대상 게시글 ID Set)
- 방문자 IP 중복: `visitor:post:{postId}:ips` (TTL 24시간)
- AI 캐싱: `ai:{agentType}:{postId}:{contentHash}`
- 구분자는 콜론(`:`)을 사용

### 방문자 통계
- Redis INCR로 원자적 카운트
- 일별 방문자는 날짜 키로 관리, TTL로 자동 만료
- 게시글별 조회수는 Set으로 IP 중복 제거 (TTL 24시간)
- 조회수 동기화 시 KEYS 명령어 금지 → `visitor:tracked-posts` Set으로 대상 관리

## 인증 규칙

- application.yml에 관리자 username/password 고정
- 로그인 시 JWT 발급
- JWT를 Authorization: Bearer 헤더로 전달
- 글 작성/수정/삭제, AI 피드백 요청 등은 인증 필수
- 글 조회, 목록, SEO 관련은 인증 불필요 (공개 API)

## DTO 규칙

- Request/Response DTO는 Java Record 사용
- Entity를 직접 응답으로 반환하지 않는다
- DTO 변환은 정적 팩토리 메서드로: `PostResponse.from(Post post)`
- Validation은 Request DTO에 어노테이션으로: `@NotBlank`, `@Size`, `@NotNull`

## SEO 규칙

- 게시글 생성 시 slug 자동 생성 (title 기반, 한글은 로마자 변환 또는 ID 조합)
- 각 게시글 메타 태그: title, description, og:title, og:description, og:image
- sitemap.xml은 게시글 생성/수정/삭제 시 갱신
- robots.txt는 정적 파일로 제공

## 절대 하지 말 것

- Entity에 `@Setter` 사용
- Entity에 `@OneToMany` (양방향 매핑) 사용
- Entity에 `cascade` 사용
- `@ManyToOne`에 `FetchType.EAGER` 사용
- Controller에 비즈니스 로직 작성
- Entity를 API 응답으로 직접 반환
- Service에서 HttpServletRequest/Response 직접 접근
- 교체 가능성 없는 Service에 인터페이스 만들기 (오버엔지니어링)
- 하드코딩된 문자열로 Redis 키 생성 (상수 또는 유틸리티 사용)
- Redis KEYS 명령어 사용 (SCAN 또는 별도 Set으로 관리)
- AI API 키를 코드에 직접 작성 (application.yml + @Value 또는 @ConfigurationProperties)
- catch (Exception e) 로 모든 예외 뭉뚱그리기
- 도메인 간 순환 의존