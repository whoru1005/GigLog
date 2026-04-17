# GigLog — 공연 팬 통합 플랫폼

공연(뮤지컬·콘서트·연극) 관람 기록 아카이빙 + 개인 통계 + 티켓팅 알림 서비스.
Spring Boot MSA (3개 서비스), PostgreSQL, Kafka, Redis, AWS 기반.

## 서비스 구조

```
stagediary/
├── performance-service/   # 공연 정보 CRUD, KOPIS API 연동, 검색
├── archive-service/       # 관람 기록, 개인 통계, 사진 업로드, 유저 관리
├── notification-service/  # 관심 공연 구독, 알림 스케줄링, FCM 발송
├── common/                # 공통 DTO, 예외, 유틸, JWT 필터
├── docker/                # docker-compose, Dockerfile
└── .github/workflows/     # CI/CD 파이프라인
```

## 빌드 & 실행

```bash
# 로컬 인프라 (PostgreSQL, Redis, Kafka)
docker-compose -f docker/docker-compose-local.yml up -d

# 전체 빌드
./gradlew clean build

# 서비스별 실행
./gradlew :performance-service:bootRun
./gradlew :archive-service:bootRun
./gradlew :notification-service:bootRun

# 전체 테스트
./gradlew test

# 특정 서비스 테스트
./gradlew :performance-service:test
```

## 기술 스택

- Java 17, Spring Boot 3.x, Gradle (멀티모듈)
- PostgreSQL 15 (서비스별 스키마 분리: performance_schema, archive_schema, notification_schema)
- Redis 7 (공연 목록 캐싱)
- Apache Kafka 3.x (서비스 간 비동기 이벤트)
- Spring Security + JWT (API Gateway 레벨 검증)
- QueryDSL 5.x (동적 검색)
- Flyway (DB 마이그레이션)
- AWS: EC2, ALB, RDS, S3, ElastiCache, CloudWatch

## 프로젝트 관리

- Jira 스크럼 보드 (프로젝트 키: `GL`, 1스프린트 = 1주, 총 8스프린트)
- Atlassian Rovo MCP 연결됨 — 이슈 생성/조회/업데이트에 활용
- 이슈 계층: Epic → Story → Task
- Jira-Git 연동: 브랜치명·커밋·PR에 이슈 키(`GL-XX`) 포함
- 스프린트 관리 상세 규칙은 `.claude/rules/jira.md` 참고

## 핵심 규칙

- 세부 코드 컨벤션, DB, Kafka, 인프라 규칙은 `.claude/rules/` 참고
- 서비스 간 동기 호출은 OpenFeign (archive → performance 공연 정보 조회만 해당)
- 그 외 서비스 간 통신은 모두 Kafka 비동기 이벤트
- 각 서비스는 자기 스키마만 접근. 다른 서비스의 테이블을 직접 조회하지 않음
- API 응답은 항상 `ApiResponse<T>` 래퍼 사용
- 모든 엔티티에 `createdAt`, `updatedAt` Auditing 적용
