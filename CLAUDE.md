# CLAUDE.md - 프로젝트 그라운드 룰

## 프로젝트 개요
- AI Agent 기반 개인 블로그 플랫폼 백엔드
- Java 21 / Spring Boot 4.0.x / Gradle

## 코드 컨벤션
- Google Java Style Guide 준수 (https://google.github.io/styleguide/javaguide.html)
- 들여쓰기: 스페이스 2칸
- 최대 줄 길이: 100자
- 클래스명: UpperCamelCase
- 메서드/변수명: lowerCamelCase
- 상수: UPPER_SNAKE_CASE
- 패키지명: 소문자만 사용

## 패키지 구조
- 기본 패키지: `com.aiblog`
- 계층형 구조: controller / service / repository / domain / dto / config

## Git 규칙
- **절대 자동으로 git add, commit, push 하지 말 것**
- 커밋 단위: 파일 1개 또는 밀접한 파일 묶음 (여러 파일을 하나의 큰 커밋으로 합치지 말 것)
- 형식: `[타입] 간결한 설명`
- 타입: feat, fix, refactor, docs, chore, test
- 기능 구현이 완료될 때마다 아래처럼 **복붙 가능한 git 명령어**를 제안할 것:
  ```
  git add src/main/java/com/aiblog/global/exception/BusinessException.java
  git commit -m "[feat] BusinessException 추가"
  ```
- build.gradle, 설정 파일 변경도 별도 커밋으로 분리

## Gradle / 설정 파일 관리 (중요)
- build.gradle은 항상 깔끔하게 유지할 것
- Spring Boot BOM이 관리하는 의존성은 **버전을 직접 명시하지 말 것** (중복 버전 금지)
- BOM이 관리하지 않는 외부 라이브러리만 버전 명시
- 의존성 추가/수정/삭제 시 반드시 웹 검색하여 확인할 것:
  - 해당 라이브러리의 최신 안정 버전인지
  - Spring Boot BOM에서 이미 관리하는 버전인지
  - 중복되거나 불필요한 의존성은 없는지
- application.yml(properties)도 불필요한 설정 없이 깔끔하게 유지
- 사용하지 않는 의존성이나 설정은 즉시 제거

## 작업 방식
- 코드 수정 전 반드시 해당 파일을 먼저 읽을 것
- 요청하지 않은 리팩토링이나 기능 추가 금지
- 새 파일 생성 시 기존 프로젝트 구조와 패턴을 따를 것
- 한국어로 소통

## 응답 언어
- 대화 및 설명: 한국어
- 코드/커밋 메시지: 한국어 허용
- 주석: 한국어 허용
