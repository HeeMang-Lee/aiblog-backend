# 시퀀스 다이어그램

## 1. 인증 흐름

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant AC as AuthController
    participant AS as AuthService
    participant JWT as JwtProvider
    participant SF as SecurityFilter

    Note over Admin,SF: 로그인 (Controller가 직접 처리)
    Admin->>AC: POST /api/auth/login (username, password)
    AC->>AS: login(username, password)
    AS->>AS: yml 고정 계정과 비교
    alt 인증 성공
        AS->>JWT: JWT 토큰 생성
        JWT-->>AS: accessToken
        AS-->>AC: TokenResponse
        AC-->>Admin: 200 OK { token }
    else 인증 실패
        AS-->>AC: UnauthorizedException
        AC-->>Admin: 401 Unauthorized
    end

    Note over Admin,SF: 인증 필요 API 호출 (SecurityFilter가 처리)
    Admin->>SF: Authorization: Bearer {token}
    SF->>JWT: 토큰 검증
    alt 유효한 토큰
        JWT-->>SF: 인증 정보
        SF->>AC: 요청 전달 (SecurityContext에 인증 정보 세팅)
        AC-->>Admin: 200 OK
    else 만료/무효 토큰
        JWT-->>SF: 검증 실패
        SF-->>Admin: 401 Unauthorized
    end
```

## 2. 게시글 작성 전체 흐름 (핵심)

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant PC as PostController
    participant PS as PostService
    participant DB as MySQL
    participant AIC as AiController
    participant AIS as AiAgentService
    participant Router as AiClientRouter
    participant Cache as Redis
    participant GPT as GPT API
    participant Claude as Claude API

    Note over Admin,Claude: Step 1: 초안 저장
    Admin->>PC: POST /api/posts { title, content, categoryId, status: DRAFT }
    PC->>PS: createPost()
    PS->>PS: slug 생성 (title 기반)
    PS->>DB: INSERT post (status=DRAFT)
    DB-->>PS: post (id=1)
    PS-->>PC: PostResponse
    PC-->>Admin: 201 Created { postId: 1 }

    Note over Admin,Claude: Step 2: AI 피드백 요청 (수동 버튼)
    Admin->>AIC: POST /api/posts/1/ai-feedback
    AIC->>AIS: requestFeedback(postId=1)
    AIS->>DB: SELECT post WHERE id=1
    DB-->>AIS: post (content)
    AIS->>AIS: contentHash = SHA-256(content)
    AIS->>Cache: GET ai:feedback:1:{hash}
    alt 캐시 적중
        Cache-->>AIS: 캐시된 AI 응답
    else 캐시 미스
        AIS->>Router: routeAndCall(prompt)
        Router->>GPT: API 호출
        alt GPT 성공
            GPT-->>Router: 응답
        else GPT 실패 → 폴백
            Router->>Claude: API 호출
            Claude-->>Router: 응답
        end
        Router-->>AIS: AI 응답
        AIS->>Cache: SET ai:feedback:1:{hash} (TTL)
        AIS->>DB: INSERT ai_result (agent_type=FEEDBACK)
    end
    AIS-->>AIC: AiFeedbackResponse
    AIC-->>Admin: 200 OK { feedback, provider }

    Note over Admin,Claude: Step 3: 초안 수정 후 발행
    Admin->>PC: PUT /api/posts/1 { content: 수정됨, status: PUBLISHED }
    PC->>PS: updatePost()
    PS->>DB: UPDATE post (status=PUBLISHED, published_at=now)
    PS-->>PC: PostResponse
    PC-->>Admin: 200 OK

    Note over Admin,Claude: Step 4: 추천 에이전트 자동 실행 (비동기)
    Note right of PS: 현재: 직접 호출 / 추후: @EventListener + @Async 이벤트 기반 전환 가능
    PS->>AIS: executeRecommendation(postId=1) [async]
    AIS->>AIS: contentHash = SHA-256(수정된 content)
    AIS->>Cache: GET ai:recommendation:1:{hash}
    alt 캐시 미스 (내용 변경됨)
        AIS->>Router: routeAndCall(추천 prompt)
        Router->>GPT: API 호출
        GPT-->>Router: 추천 목록
        Router-->>AIS: AI 응답
        AIS->>Cache: SET ai:recommendation:1:{hash} (TTL)
        AIS->>DB: INSERT ai_result (agent_type=RECOMMENDATION)
        AIS->>DB: DELETE + INSERT post_recommendation
    end
```

## 3. 게시글 조회 + 방문자 통계

```mermaid
sequenceDiagram
    actor User as 방문자
    participant PC as PostController
    participant PS as PostService
    participant VS as VisitorService
    participant DB as MySQL
    participant Redis as Redis

    User->>PC: GET /api/posts/{slug}
    PC->>PS: getPostBySlug(slug)
    PS->>DB: SELECT post WHERE slug = ?
    DB-->>PS: post

    par 방문자 통계 (병렬)
        PC->>VS: recordVisit(postId, clientIp)
        VS->>Redis: SISMEMBER visitor:post:{postId}:ips {ip}
        alt 신규 방문자 (중복 아님)
            VS->>Redis: SADD visitor:post:{postId}:ips {ip}
            VS->>Redis: EXPIRE visitor:post:{postId}:ips 86400 (24시간 TTL)
            VS->>Redis: INCR visitor:post:{postId}
            VS->>Redis: INCR visitor:total
            VS->>Redis: INCR visitor:today:{yyyyMMdd}
            VS->>Redis: SADD visitor:tracked-posts {postId}
        end
    end

    PS-->>PC: PostResponse (+ viewCount)
    PC-->>User: 200 OK { post, viewCount }
```

## 4. 방문자 통계 조회

```mermaid
sequenceDiagram
    actor User as 방문자
    participant VC as VisitorController
    participant VS as VisitorService
    participant Redis as Redis

    User->>VC: GET /api/visitors/stats
    VC->>VS: getVisitorStats()
    par Redis 다중 조회
        VS->>Redis: GET visitor:total
        VS->>Redis: GET visitor:today:{yyyyMMdd}
    end
    Redis-->>VS: totalCount, todayCount
    VS-->>VC: VisitorStatsResponse
    VC-->>User: 200 OK { total, today }
```

## 5. 파일 업로드 + 게시글 첨부

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant AC as AttachmentController
    participant FS as FileStorageService
    participant AS as AttachmentService
    participant DB as MySQL
    participant Disk as 로컬 파일시스템

    Admin->>AC: POST /api/posts/{postId}/attachments (multipart file)
    AC->>FS: store(file, "posts/{postId}")
    FS->>FS: 파일명 UUID 생성
    FS->>Disk: 파일 저장
    Disk-->>FS: 저장 경로
    FS-->>AC: filePath

    AC->>AS: createAttachment(postId, type, filePath, originalFilename, fileSize)
    AS->>DB: INSERT attachment
    DB-->>AS: attachment
    AS-->>AC: AttachmentResponse
    AC-->>Admin: 201 Created { id, url, type }
```

## 6. 외부 링크 첨부

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant AC as AttachmentController
    participant AS as AttachmentService
    participant DB as MySQL

    Admin->>AC: POST /api/posts/{postId}/attachments/link { url, type: LINK }
    AC->>AS: createLinkAttachment(postId, url)
    AS->>DB: INSERT attachment (type=LINK, url=외부URL)
    DB-->>AS: attachment
    AS-->>AC: AttachmentResponse
    AC-->>Admin: 201 Created { id, url, type: LINK }
```

## 7. AI 추천 수동 갱신 (게시글 수정 후)

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant AIC as AiController
    participant AIS as AiAgentService
    participant Router as AiClientRouter
    participant Cache as Redis
    participant DB as MySQL
    participant GPT as GPT API

    Admin->>AIC: POST /api/posts/{postId}/ai-recommendation/refresh
    AIC->>AIS: refreshRecommendation(postId)
    AIS->>DB: SELECT post WHERE id = ?
    DB-->>AIS: post (content)
    AIS->>AIS: contentHash = SHA-256(content)
    AIS->>Cache: GET ai:recommendation:{postId}:{hash}
    alt 캐시 적중 (내용 변경 없음)
        Cache-->>AIS: 캐시된 추천 결과
        AIS-->>AIC: "내용 변경 없음, 기존 추천 유지"
    else 캐시 미스 (내용 변경됨)
        AIS->>Router: routeAndCall(추천 prompt)
        Router->>GPT: API 호출
        GPT-->>Router: 추천 목록
        Router-->>AIS: AI 응답
        AIS->>Cache: SET ai:recommendation:{postId}:{hash} (TTL)
        AIS->>DB: INSERT ai_result
        AIS->>DB: DELETE post_recommendation WHERE post_id = ?
        AIS->>DB: INSERT post_recommendation (새 추천 관계)
        AIS-->>AIC: 갱신된 추천 결과
    end
    AIC-->>Admin: 200 OK { recommendations }
```

## 8. 카테고리 CRUD

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant CC as CategoryController
    participant CS as CategoryService
    participant DB as MySQL

    Note over Admin,DB: 카테고리 생성
    Admin->>CC: POST /api/categories { name: "Java" }
    CC->>CS: createCategory("Java")
    CS->>DB: SELECT EXISTS WHERE name = "Java"
    alt 이미 존재
        DB-->>CS: true
        CS-->>CC: DuplicateCategoryNameException
        CC-->>Admin: 409 Conflict
    else 신규
        CS->>CS: slug 생성 ("java")
        CS->>DB: INSERT category
        DB-->>CS: category
        CS-->>CC: CategoryResponse
        CC-->>Admin: 201 Created
    end

    Note over Admin,DB: 카테고리 목록 조회 (공개)
    Admin->>CC: GET /api/categories
    CC->>CS: getCategoryList()
    CS->>DB: SELECT * FROM category ORDER BY display_order
    DB-->>CS: categories
    CS-->>CC: List<CategoryResponse>
    CC-->>Admin: 200 OK { categories }
```

## 9. Redis → MySQL 조회수 동기화 (배치)

```mermaid
sequenceDiagram
    participant Scheduler as 스케줄러 (@Scheduled)
    participant VS as VisitorService
    participant Redis as Redis
    participant DB as MySQL

    Note over Scheduler,DB: 주기적 실행 (예: 5분마다)
    Scheduler->>VS: syncViewCountsToDb()
    Note right of VS: KEYS 대신 tracked-posts Set 사용 (KEYS는 Redis 블로킹 위험)
    VS->>Redis: SMEMBERS visitor:tracked-posts
    Redis-->>VS: [1, 2, 3, ...] (동기화 대상 게시글 ID)
    loop 각 게시글별
        VS->>Redis: GET visitor:post:{postId}
        Redis-->>VS: count
        VS->>DB: UPDATE post SET view_count = ? WHERE id = ?
    end
    Note over Scheduler,DB: 일별 IP Set 만료
    Note right of VS: visitor:post:{postId}:ips는 24시간 TTL로 자동 만료
```

## 10. AI 결과 조회

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant AIC as AiController
    participant AIS as AiAgentService
    participant DB as MySQL

    Note over Admin,DB: 피드백 이력 조회
    Admin->>AIC: GET /api/posts/{postId}/ai-results?type=FEEDBACK
    AIC->>AIS: getAiResults(postId, FEEDBACK)
    AIS->>DB: SELECT * FROM ai_result WHERE post_id = ? AND agent_type = ?
    DB-->>AIS: ai_results
    AIS-->>AIC: List<AiResultResponse>
    AIC-->>Admin: 200 OK { results }

    Note over Admin,DB: 전체 AI 결과 조회 (피드백 + 추천)
    Admin->>AIC: GET /api/posts/{postId}/ai-results
    AIC->>AIS: getAiResults(postId)
    AIS->>DB: SELECT * FROM ai_result WHERE post_id = ?
    DB-->>AIS: ai_results
    AIS-->>AIC: List<AiResultResponse>
    AIC-->>Admin: 200 OK { results }
```

## API 엔드포인트 요약

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/auth/login` | X | 관리자 로그인 |
| GET | `/api/categories` | X | 카테고리 목록 |
| POST | `/api/categories` | O | 카테고리 생성 |
| PUT | `/api/categories/{id}` | O | 카테고리 수정 |
| DELETE | `/api/categories/{id}` | O | 카테고리 삭제 |
| GET | `/api/posts` | X | 게시글 목록 (발행된 것만) |
| GET | `/api/posts/{slug}` | X | 게시글 상세 (slug) |
| GET | `/api/categories/{id}/posts` | X | 카테고리별 게시글 목록 |
| POST | `/api/posts` | O | 게시글 생성 |
| PUT | `/api/posts/{id}` | O | 게시글 수정 |
| DELETE | `/api/posts/{id}` | O | 게시글 삭제 |
| POST | `/api/posts/{id}/attachments` | O | 파일 첨부 |
| POST | `/api/posts/{id}/attachments/link` | O | 링크 첨부 |
| DELETE | `/api/attachments/{id}` | O | 첨부 삭제 |
| POST | `/api/posts/{id}/ai-feedback` | O | AI 피드백 요청 |
| GET | `/api/posts/{id}/ai-results` | O | AI 결과 조회 (피드백/추천 이력) |
| GET | `/api/posts/{id}/ai-results?type={type}` | O | AI 결과 타입별 조회 |
| POST | `/api/posts/{id}/ai-recommendation/refresh` | O | AI 추천 수동 갱신 |
| GET | `/api/posts/{id}/recommendations` | X | 추천 글 목록 |
| GET | `/api/visitors/stats` | X | 방문자 통계 |
| GET | `/sitemap.xml` | X | 사이트맵 |
| GET | `/robots.txt` | X | 로봇 파일 |
