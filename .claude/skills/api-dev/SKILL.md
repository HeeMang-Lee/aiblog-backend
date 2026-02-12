---
name: api-dev
description: AI 블로그 백엔드 API 개발 워크플로우. API 구현, 엔티티 설계, 예외 처리 등 백엔드 개발 작업 시 사용.
---

# AI Blog Backend - API Development Skill

> 이 스킬은 AI Agent 기반 개인 블로그 플랫폼 백엔드 개발 시 따라야 할 워크플로우와 규칙을 정의합니다.

## 기술 스택

- Java 21 / Spring Boot 4.0.x / Gradle
- MySQL (메인 DB) / Redis (캐싱, 방문자 통계)
- AI API: GPT (기본) → Claude (폴백), 순서 변경 가능
- 파일 저장: 로컬 (FileStorageService 인터페이스로 추상화)
- 인증: Spring Security + JWT + application.yml 고정 관리자 계정

## 워크플로우

모든 API 개발은 아래 순서를 반드시 따른다:

### Step 1: 요구사항 확인
- 구현할 기능의 범위를 사용자에게 확인받는다
- 기존 코드와 충돌 여부를 먼저 파악한다
- 불명확한 부분은 구현 전에 반드시 질문한다

### Step 2: 설계
- 어떤 레이어에 어떤 클래스를 만들지 목록을 먼저 제시한다
- DB 스키마 변경이 필요하면 엔티티 변경사항을 먼저 보여준다
- 사용자 승인 후 구현에 들어간다

### Step 3: 구현 순서
1. Entity / DTO 정의
2. Repository 인터페이스
3. Service 비즈니스 로직
4. Controller (REST API)
5. 예외 처리 연결

### Step 4: 검증
- 컴파일 에러 없는지 확인
- 기존 코드와의 의존성 확인
- API 요청/응답 예시 제공

## 레이어드 아키텍처 규칙

```
Controller → Service → Repository → Entity
    ↓           ↓
 Request/    비즈니스
 Response    로직만
  DTO
```

### Controller
- HTTP 요청/응답만 담당한다
- 비즈니스 로직을 절대 넣지 않는다
- `@RestController` + `@RequestMapping` 사용
- 메서드별 `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Request DTO로 받고, Response DTO로 응답한다

### Service
- `@Service` + `@Transactional` 사용
- 인터페이스 없이 구체 클래스로 구현한다 (단일 구현체면 인터페이스 불필요)
- 단, AI API나 파일 저장소처럼 구현체가 교체될 수 있는 경우 인터페이스를 사용한다
- 다른 Service를 주입받을 수 있지만, 순환 의존은 금지

### Repository
- Spring Data JPA 사용
- 커스텀 쿼리는 `@Query` 또는 QueryDSL
- 네이밍: `findByXxx`, `existsByXxx` 등 Spring Data 컨벤션을 따른다

### Entity
- `@Entity` + `@Table(name = "...")` 명시
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `@Column` 어노테이션으로 nullable, length 등 명시
- BaseEntity로 createdAt, updatedAt 공통 관리
- Lombok: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@Builder`
- `@Setter` 사용 금지 → 의미 있는 메서드명으로 상태 변경

## 네이밍 컨벤션

### 패키지 구조
```
com.aiblog
├── domain
│   ├── post          # 게시글
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   ├── category      # 카테고리
│   ├── visitor        # 방문자 통계
│   ├── seo            # SEO
│   └── ai             # AI 에이전트
│       ├── agent       # 에이전트 구현체들
│       ├── client      # AI API 클라이언트 (GPT, Claude)
│       └── cache       # AI 응답 캐싱
├── global
│   ├── config
│   ├── security
│   ├── exception
│   ├── response
│   └── storage        # 파일 저장소 추상화
└── infra
    └── redis
```

### 클래스 네이밍
- Controller: `PostController`, `CategoryController`
- Service: `PostService`, `AiAgentService`
- Repository: `PostRepository`
- Entity: `Post`, `Category`
- Request DTO: `PostCreateRequest`, `PostUpdateRequest`
- Response DTO: `PostResponse`, `PostListResponse`
- Exception: `PostNotFoundException`, `AiApiException`

### 메서드 네이밍
- 생성: `createXxx`
- 조회(단건): `getXxx`, `getXxxById`
- 조회(목록): `getXxxList`, `getXxxListByCategoryId`
- 수정: `updateXxx`
- 삭제: `deleteXxx`

### API 경로
- REST 컨벤션: 복수형 명사, kebab-case
- 예시: `/api/posts`, `/api/categories`, `/api/posts/{postId}/ai-feedback`

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
// 비즈니스 예외 베이스
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
}

// ErrorCode enum
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

## AI 에이전트 설계 규칙

### 클라이언트 추상화
```java
public interface AiClient {
    String call(String prompt);
    String getProviderName();
}
// 구현체: GptClient, ClaudeClient
```

### Fallback 전략
```java
public interface AiClientRouter {
    String routeAndCall(String prompt);
    // 내부: 설정된 순서대로 시도, 실패 시 다음 클라이언트로 폴백
}
```

### 에이전트 구조
```java
public interface AiAgent {
    AiAgentResponse execute(AiAgentRequest request);
    AiAgentType getType();
}
// 구현체: FeedbackAgent, RecommendationAgent
// 새 에이전트 추가 시 AiAgent 구현 + AiAgentType에 enum 추가
```

### AI 응답 캐싱
- 캐시 키: `ai:{agentType}:{postId}:{contentHash}`
- contentHash: 게시글 내용의 SHA-256 해시
- 내용 변경(해시 변경) 시 자동 캐시 무효화
- TTL 기반 만료 적용

## Redis 사용 규칙

### 키 네이밍
- 방문자 통계: `visitor:total`, `visitor:today:{yyyyMMdd}`, `visitor:post:{postId}`
- AI 캐싱: `ai:{agentType}:{postId}:{contentHash}`
- 구분자는 콜론(`:`)을 사용

### 방문자 통계
- Redis INCR로 원자적 카운트
- 일별 방문자는 날짜 키로 관리, TTL로 자동 만료
- 게시글별 조회수는 Set으로 IP 중복 제거 고려

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

## 파일 저장소 추상화

```java
public interface FileStorageService {
    String store(MultipartFile file, String directory);
    void delete(String filePath);
    Resource load(String filePath);
}
// 현재 구현체: LocalFileStorageService
// 추후: S3FileStorageService, R2FileStorageService
```

## SEO 규칙

- 게시글 생성 시 slug 자동 생성 (title 기반, 한글은 로마자 변환 또는 ID 조합)
- 각 게시글 메타 태그: title, description, og:title, og:description, og:image
- sitemap.xml은 게시글 생성/수정/삭제 시 갱신
- robots.txt는 정적 파일로 제공

## 절대 하지 말 것

- Entity에 `@Setter` 사용
- Controller에 비즈니스 로직 작성
- Entity를 API 응답으로 직접 반환
- Service에서 HttpServletRequest/Response 직접 접근
- 하드코딩된 문자열로 Redis 키 생성 (상수 또는 유틸리티 사용)
- AI API 키를 코드에 직접 작성 (application.yml + @Value 또는 @ConfigurationProperties)
- catch (Exception e) 로 모든 예외 뭉뚱그리기
