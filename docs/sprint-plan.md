# 스프린트 상세 계획

이 문서는 Jira 이슈 생성 시 참고용입니다.
Claude Code에서 `@docs/sprint-plan.md`로 참조하여 사용합니다.

---

## Sprint 1 (Week 1): 프로젝트 초기 설정

**EP01 프로젝트 기반 구축**

Story: 개발 환경 구축
- Task: Gradle 멀티모듈 프로젝트 생성
- Task: common 모듈 (ApiResponse, 예외처리, JWT 필터)
- Task: Docker Compose 로컬 환경 (PostgreSQL, Redis, Kafka)
- Task: 서비스별 application.yml 설정

Story: 인증 시스템 구현
- Task: Spring Security + JWT 설정
- Task: 소셜 로그인 연동
- Task: app_user 엔티티 및 마이그레이션

## Sprint 2 (Week 2): 인프라 + Kafka 기반

**EP05 인프라 & CI/CD**

Story: AWS 환경 구축
- Task: EC2 인스턴스 3개 + ALB 설정
- Task: RDS PostgreSQL + 스키마 분리
- Task: ElastiCache Redis 설정
- Task: S3 버킷 생성 + IAM 역할

Story: CI/CD 파이프라인 구축
- Task: GitHub Actions 워크플로우 (서비스별)
- Task: Docker 이미지 빌드 + EC2 배포 자동화

**EP01 프로젝트 기반 구축**

Story: Kafka 기반 인프라 설정
- Task: EC2에 Kafka 단일 브로커 설치
- Task: 토픽 생성 스크립트 작성
- Task: 프로듀서/컨슈머 기본 연동 테스트

## Sprint 3 (Week 3): 공연 정보 - 핵심 CRUD

**EP02 공연 정보 서비스**

Story: 공연 정보 등록/조회
- Task: Performance, Venue 엔티티 및 마이그레이션
- Task: 공연 CRUD API (Controller, Service, Repository)
- Task: 공연장 CRUD API
- Task: 스케줄/캐스팅 엔티티 및 API

Story: KOPIS 공공데이터 연동
- Task: KOPIS API 클라이언트 구현
- Task: 데이터 동기화 스케줄러 (일 1회)
- Task: 중복 방지 로직 (kopis_id 기반)

## Sprint 4 (Week 4): 공연 정보 - 검색/캐싱

**EP02 공연 정보 서비스**

Story: 장르별 공연 검색
- Task: QueryDSL 동적 검색 구현
- Task: Cursor 기반 페이지네이션
- Task: 검색 API 통합 테스트

Story: 공연 목록 캐싱
- Task: Redis Cache Aside 패턴 적용
- Task: 캐시 무효화 정책 (TTL + 이벤트 기반)
- Task: 캐시 적용 전/후 성능 비교 기록

## Sprint 5 (Week 5): 관람 기록 서비스

**EP03 관람 기록 서비스**

Story: 관람 기록 아카이빙
- Task: Archive, ArchivePhoto 엔티티 및 마이그레이션
- Task: 관람 기록 CRUD API
- Task: S3 Presigned URL 사진 업로드
- Task: archive.created Kafka 이벤트 발행

Story: 개인 통계 조회
- Task: 월별 관람 횟수 집계 쿼리 (QueryDSL)
- Task: 장르 분포, 자주 방문한 공연장 통계
- Task: 통계 API + 단위 테스트

## Sprint 6 (Week 6): 알림 서비스 + Kafka 연동

**EP04 알림 서비스**

Story: 관심 공연 구독
- Task: Subscription 엔티티 및 API
- Task: subscription.created Kafka 이벤트 발행
- Task: schedule.created 이벤트 컨슈머 → 알림 스케줄 자동 생성

Story: 푸시 알림 발송
- Task: FCM 연동 (Firebase Admin SDK)
- Task: 알림 스케줄러 (D-1일, 1시간 전)
- Task: FCM 토큰 관리 API

Story: 이벤트 신뢰성 보장
- Task: Transactional Outbox 패턴 구현
- Task: Outbox 폴링 스케줄러
- Task: DLQ 토픽 설정 + 재시도 정책
- Task: 멱등성 처리 (processed_events 테이블)

## Sprint 7 (Week 7): 테스트 + 모니터링

**EP06 테스트 & 안정화**

Story: 통합 테스트 작성
- Task: Testcontainers 기반 통합 테스트 환경
- Task: 서비스 간 이벤트 흐름 E2E 테스트
- Task: API 인증/인가 통합 테스트

Story: 부하 테스트
- Task: K6 또는 JMeter 스크립트 작성
- Task: 병목 지점 식별 + 튜닝 기록

Story: 모니터링 구축
- Task: CloudWatch 대시보드 설정
- Task: 알람 설정 (CPU, 커넥션, DLQ)
- Task: 로그 수집 파이프라인

## Sprint 8 (Week 8): 문서화 + 포트폴리오

**EP06 테스트 & 안정화**

Story: API 문서화
- Task: SpringDoc OpenAPI (Swagger) 적용
- Task: 주요 API 설명 및 예시 추가

Story: 포트폴리오 정리
- Task: README.md 작성 (프로젝트 소개, 아키텍처 다이어그램)
- Task: 기술 선택 근거 문서 (ADR)
- Task: 트러블슈팅 기록 정리
- Task: Jira 번다운 차트/스프린트 리포트 캡처
