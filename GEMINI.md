# GigLog — 공연 팬 통합 플랫폼

공연(뮤지컬·콘서트·연극) 관람 기록 아카이빙 + 개인 통계 + 티켓팅 알림 서비스.
Spring Boot, PostgreSQL, Kafka, Redis, AWS 기반.

## 프로젝트 개요

- **프로젝트 명:** GigLog (기존 StageDiary에서 변경)
- **기술 스택:** Java 17, Spring Boot 3.x, Gradle, PostgreSQL 15, Redis 7, Apache Kafka 3.x
- **기본 패키지:** `com.giglog`

## 핵심 규칙 (Engineering Standards)

### 1. 코드 컨벤션 & 구조
- **패키지 구조:** `com.giglog.{서비스명}` 하위에 `controller`, `service`, `repository`, `domain`, `dto`, `config`, `exception`, `kafka`, `util` 구성.
- **네이밍:** 클래스는 `PascalCase` (역할 접미사 필수), 메서드/변수는 `camelCase` (동사 시작), 상수는 `UPPER_SNAKE_CASE`.
- **API 응답:** 모든 응답은 `ApiResponse<T>` 래퍼 클래스를 사용.
- **의존성 주입:** `@Autowired` 금지, 생성자 주입(`@RequiredArgsConstructor`)만 사용.
- **엔티티 보호:** 엔티티를 API 응답으로 직접 반환 금지, 반드시 DTO로 변환. `Optional.get()` 대신 `orElseThrow()` 사용.

### 2. Spring Boot & JPA
- **엔티티:** `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder` 사용. PK는 `IDENTITY` 전략.
- **Auditing:** `BaseTimeEntity`를 상속받아 `createdAt`, `updatedAt` 자동 관리.
- **지연 로딩:** `FetchType.LAZY` 기본 사용. 양방향 매핑 지양.
- **트랜잭션:** 서비스 레이어에서 `@Transactional(readOnly = true)` 기본 설정, 쓰기 메서드에만 `@Transactional` 적용.
- **QueryDSL:** 커스텀 인터페이스/구현체 분리 구조 준수. N+1 방지를 위한 `fetchJoin()` 필수.

### 3. 데이터베이스 (PostgreSQL)
- **네이밍:** 테이블/컬럼은 `snake_case` 단수형 사용.
- **마이그레이션:** Flyway 사용 (`src/main/resources/db/migration/`). 버전 관리 엄수.
- **페이지네이션:** Cursor 기반 (`Slice<T>`) 우선 사용.

### 4. 테스트 전략
- **JUnit 5 + Mockito** 기반. `should_결과_when_조건` 네이밍 패턴 준수.
- **단위 테스트:** Service, Util 대상. Spring 컨텍스트 로드 없이 `@ExtendWith(MockitoExtension.class)` 사용.
- **통합 테스트:** `@SpringBootTest` + Testcontainers(PostgreSQL, Redis) 활용.
- **커버리지:** Service 레이어 80% 이상 목표.

### 5. Git 워크플로우
- **브랜치:** `main` (배포), `develop` (개발), `feature/{서비스}/{기능}` (기능 개발) 전략.
- **커밋 메시지:** `{타입}({범위}): {설명}` (한글 허용).
  - 타입: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `infra`, `style`.
- PR을 통한 코드 리뷰 및 머지 프로세스 준수.

### 6. 인프라 & 보안
- **Docker:** 멀티스테이지 빌드 Dockerfile 사용. `docker-compose-local.yml`로 로컬 인프라 구성.
- **CI/CD:** GitHub Actions 기반 서비스별 독립 배포 파이프라인.
- **보안:** JWT 인증 (Header Authorization). 비밀번호 BCrypt 암호화. SQL Injection/XSS 방지 로직 적용.
- **환경변수:** 민감 정보는 `.env` 또는 시스템 환경변수로 관리 (yml 하드코딩 금지).

### 7. Kafka 이벤트
- **토픽 네이밍:** `{도메인}.{엔티티}.{동작}` (예: `performance.schedule.created`).
- **페이로드:** `BaseEvent` 공통 구조 사용 (eventId, eventType, timestamp 포함).
- **신뢰성:** Transactional Outbox 패턴 적용 (필요 시). 멱등성 처리 필수.

---
*세부 사항은 `.claude/rules/` 하위의 개별 규칙 파일들을 참고하십시오. Gemini는 이 규칙들을 최우선으로 준수하여 코드를 작성합니다.*
