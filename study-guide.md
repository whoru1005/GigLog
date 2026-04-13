# 선행 학습 가이드 — 프로젝트 시작 전 꼭 공부해야 할 것들

이 프로젝트에서 처음 사용하는 기술이 3가지(PostgreSQL, Kafka, MSA)입니다.
Claude Code가 코드를 생성해줄 수는 있지만, **왜 이렇게 동작하는지 이해하지 못하면
면접에서 바로 드러나고, 트러블슈팅도 못 합니다.**

아래 항목들은 "나중에 필요하면 보자"가 아니라 **코드를 작성하기 전에 반드시 먼저 공부해야 할 것들**입니다.

---

## 1. Apache Kafka 기본 개념 (최우선, 1주차 전에 완료)

### 왜 공부해야 하는가
Kafka는 단순한 메시지큐가 아닙니다. RabbitMQ와 근본적으로 다른 설계 철학을 가지고 있고,
이 차이를 모르면 "왜 Kafka를 선택했냐"는 면접 질문에 답할 수 없습니다.

### 공부해야 할 핵심 개념 (순서대로)

**1단계: 아키텍처 이해 (2~3일)**
- 브로커(Broker), 토픽(Topic), 파티션(Partition)의 관계
- 프로듀서가 메시지를 보낼 때 파티션이 어떻게 결정되는지 (파티션 키)
- 컨슈머 그룹(Consumer Group)이 왜 필요하고, 리밸런싱이 뭔지
- 오프셋(Offset)이 뭔지, 컨슈머가 어디까지 읽었는지 어떻게 추적하는지
- Kafka가 메시지를 디스크에 저장한다는 것의 의미 (RabbitMQ와의 차이)

**2단계: 실습 (2~3일)**
- 로컬에 Docker로 Kafka 단일 브로커 띄우기
- kafka-console-producer / kafka-console-consumer로 메시지 주고받기
- Spring Boot에서 KafkaTemplate으로 메시지 발행, @KafkaListener로 소비
- 같은 컨슈머 그룹의 인스턴스 2개를 띄워서 파티션 분배 확인

**3단계: 신뢰성 보장 패턴 (프로젝트 6주차 전에)**
- At-least-once / At-most-once / Exactly-once 차이
- 멱등성(Idempotency)이 왜 필요한지 직접 체감
- Transactional Outbox 패턴: DB 저장과 이벤트 발행의 원자성 문제
- Dead Letter Queue(DLQ): 처리 실패 메시지를 어떻게 격리하는가

### 추천 학습 자료
- **영상 (개념 잡기):** 유튜브 "카프카 조금 아는 척하기" (최범균) — 한국어, 40분, 핵심만 설명
- **공식 문서 (필수):** https://kafka.apache.org/documentation/#gettingStarted
  - Introduction, Design, Implementation 섹션 순서대로 읽기
  - 전부 읽을 필요 없고, 위에 나열한 개념이 나오는 부분만 집중
- **실습:** Confluent Developer 튜토리얼 (https://developer.confluent.io/get-started/spring-boot/)
  - Spring Boot + Kafka 기본 연동을 step by step으로 안내
- **심화 (Outbox 패턴):** 구글에 "transactional outbox pattern spring boot" 검색
  - Debezium CDC 방식 vs 폴링 방식 비교 글을 2~3개 읽어보기
  - 이 프로젝트에서는 폴링 방식(Spring Scheduler)을 쓸 예정이지만,
    Debezium 방식도 알아두면 면접에서 "다른 방식도 검토했다"고 말할 수 있음

### 면접 대비 자기 점검 질문
- "Kafka와 RabbitMQ의 차이를 설명해주세요" → 풀/푸시 모델, 메시지 보존, 컨슈머 그룹 차이
- "파티션 수를 늘리면 어떤 장점과 단점이 있나요?"
- "컨슈머가 메시지를 처리하다 죽으면 어떻게 되나요?" → 오프셋 커밋 시점, 리밸런싱
- "Exactly-once를 보장하려면 어떻게 해야 하나요?"

---

## 2. PostgreSQL (MySQL과의 차이 중심, 1주차에 완료)

### 왜 공부해야 하는가
MySQL 경험이 있으므로 SQL 자체는 문제없지만, PostgreSQL만의 특성을 모르면
"왜 MySQL 대신 PostgreSQL을 선택했냐"는 질문에 답할 수 없습니다.

### 공부해야 할 핵심 개념

**MySQL과 다른 점 (반드시 정리)**
- MVCC 구현 차이: MySQL은 Undo Log, PostgreSQL은 튜플 버전 관리
- VACUUM이 뭔지, 왜 필요한지 (PostgreSQL의 dead tuple 문제)
- 시퀀스(Sequence) 기반 auto-increment vs MySQL의 AUTO_INCREMENT
- JSONB 타입: JSON과 JSONB의 차이, 인덱싱 가능 여부
  → 이 프로젝트에서 Kafka Outbox 이벤트 payload를 JSONB로 저장함
- Partial Index (부분 인덱스): WHERE 조건이 붙은 인덱스
  → 이 프로젝트의 notification_schedule 테이블에서 status='PENDING'인 행만 인덱싱
- 스키마(Schema) 개념: MySQL의 database ≠ PostgreSQL의 schema
  → MSA에서 하나의 RDS에 서비스별 스키마 분리할 때 이 개념을 정확히 알아야 함

### 추천 학습 자료
- **영상:** 유튜브 "PostgreSQL vs MySQL" 비교 영상 아무거나 1개 (개요 잡기)
- **공식 문서:**
  - JSONB: https://www.postgresql.org/docs/current/datatype-json.html
  - Partial Index: https://www.postgresql.org/docs/current/indexes-partial.html
- **실습:** 로컬 Docker에 PostgreSQL 띄우고:
  1. JSONB 컬럼에 데이터 넣고 `->>` 연산자로 쿼리해보기
  2. Partial Index 생성 후 EXPLAIN ANALYZE로 실행계획 확인
  3. 같은 쿼리를 일반 인덱스 vs Partial Index로 비교

### 면접 대비 자기 점검 질문
- "PostgreSQL을 선택한 이유가 뭔가요?" → JSONB, Partial Index, 스키마 분리
- "VACUUM이 뭔가요? Autovacuum 설정은 어떻게 했나요?"
- "JSONB에 인덱스를 걸 수 있나요? 어떤 종류의 인덱스가 있나요?" → GIN 인덱스

---

## 3. MSA 설계 원칙 (2주차 전에 완료)

### 왜 공부해야 하는가
"MSA를 했다"고 이력서에 쓰면, 면접관은 반드시 "왜 MSA를 선택했고,
모놀리식과 비교해서 어떤 트레이드오프가 있었냐"를 물어봅니다.
코드를 짜기 전에 이론적 배경을 반드시 이해해야 합니다.

### 공부해야 할 핵심 개념

**설계 원칙**
- Bounded Context (경계된 맥락): DDD에서 서비스를 나누는 기준
- 데이터 소유권: 각 서비스가 자기 DB만 접근하는 이유
- 서비스 간 통신: 동기(REST/gRPC) vs 비동기(이벤트) 각각의 장단점
- CAP 정리: 분산 시스템에서 일관성(C), 가용성(A), 분할 허용(P)을 동시에 만족할 수 없는 이유

**분산 트랜잭션**
- 2PC(Two-Phase Commit)가 뭔지, 왜 MSA에서 쓰기 어려운지
- Saga 패턴: Choreography vs Orchestration 차이
  → 이 프로젝트에서는 Choreography를 사용하는데, 왜 이걸 선택했는지 설명할 수 있어야 함
- 보상 트랜잭션(Compensating Transaction)의 개념

**장애 대응 패턴**
- Circuit Breaker 패턴: 장애 전파를 막는 방법
- Retry + Backoff: 재시도 전략
- Fallback: 대체 응답

### 추천 학습 자료
- **책 (강력 추천):** "마이크로서비스 패턴" (크리스 리처드슨 저, 위키북스)
  - 전부 읽을 필요 없음. 아래 챕터만 집중:
  - 1장 (모놀리식의 문제), 3장 (서비스 간 통신), 4장 (Saga 패턴), 11장 (배포)
- **무료 대안:** 마틴 파울러 블로그의 MSA 관련 글
  - "Microservices" (https://martinfowler.com/articles/microservices.html)
  - "Bounded Context" 검색
- **영상:** 유튜브 "MSA 쉽게 이해하기" 류의 한국어 영상으로 개요 잡기
- **Saga 패턴 심화:** 구글에 "saga pattern choreography spring boot example" 검색
  - 실제 Spring Boot 코드로 구현한 예제를 2~3개 비교해서 읽기

### 면접 대비 자기 점검 질문
- "모놀리식 대비 MSA의 장단점을 말해주세요"
- "서비스를 어떤 기준으로 나눴나요?" → Bounded Context, 변경 주기, 배포 독립성
- "서비스 간 데이터 정합성은 어떻게 보장하나요?" → Saga, 최종 일관성(Eventual Consistency)
- "Choreography Saga vs Orchestration Saga 차이는?"

---

## 4. Docker 네트워킹 (2주차, 인프라 셋업 시)

### 왜 공부해야 하는가
MSA에서 서비스 3개 + Kafka + PostgreSQL + Redis를 Docker로 띄우면,
컨테이너 간 통신 문제가 반드시 발생합니다. "왜 연결이 안 되지?"에서
3~4시간을 날리는 경우가 흔합니다.

### 공부해야 할 핵심 개념
- Docker 네트워크 모드: bridge, host, none 차이
- docker-compose에서 서비스 이름으로 통신하는 원리 (내장 DNS)
- 포트 매핑(호스트:컨테이너)과 컨테이너 간 직접 통신의 차이
- Kafka의 ADVERTISED_LISTENERS 설정이 왜 중요한지
  → Docker 환경에서 Kafka 연결 실패의 90%가 이 설정 문제

### 추천 학습 자료
- **Docker 공식 문서:** Networking overview
- **실습:** docker-compose로 Spring Boot 앱 2개 + PostgreSQL을 띄우고,
  앱 간 HTTP 호출이 되는지 확인
- **Kafka 관련:** "kafka docker advertised listeners" 검색해서
  블로그 글 1~2개 읽기 (Robin Moffatt의 글이 특히 좋음)

---

## 5. Spring Cloud / 서비스 간 통신 (3주차 전에)

### 왜 공부해야 하는가
서비스 간 REST 호출에 OpenFeign을 사용하는데,
단순 HTTP 호출과 Feign의 차이, 그리고 장애 시 대응을 이해해야 합니다.

### 공부해야 할 핵심 개념
- OpenFeign: 인터페이스 선언만으로 HTTP 클라이언트를 생성하는 원리
- Resilience4j: Circuit Breaker, Retry, Rate Limiter
  → Feign + Resilience4j 조합으로 archive-service가 performance-service 호출 실패 시 대응
- Service Discovery 개념: 이 프로젝트에서는 쓰지 않지만,
  "왜 안 썼냐"를 설명하려면 Eureka/Consul이 뭔지는 알아야 함

### 추천 학습 자료
- **Spring Cloud OpenFeign 공식 문서:** Getting Started 섹션
- **Resilience4j 공식 문서:** CircuitBreaker, Retry 모듈
- **실습:** 간단한 Spring Boot 앱 2개를 만들어서:
  1. Feign으로 호출 성공 케이스 확인
  2. 호출 대상 서비스를 내린 상태에서 Circuit Breaker 동작 확인

---

## 6. 캐싱 전략 (4주차 전에)

### 왜 공부해야 하는가
Redis를 "그냥 캐시로 쓴다"고 하면 면접에서 바로 꼬리 질문이 옵니다.
"캐시 무효화는 어떻게 하냐", "캐시 스탬피드는 뭐냐" 같은 질문에 답할 수 있어야 합니다.

### 공부해야 할 핵심 개념
- Cache Aside 패턴: 읽기 시 캐시 확인 → miss면 DB 조회 → 캐시에 저장
- Write Through vs Write Behind 패턴과의 차이
- 캐시 무효화 전략: TTL 기반 vs 이벤트 기반(Kafka 이벤트로 무효화)
- 캐시 스탬피드(Cache Stampede): 캐시 만료 시 동시 요청이 DB를 때리는 문제
  → 해결법: 뮤텍스 락, 사전 갱신(early refresh)
- Redis 직렬화: Java 객체를 Redis에 저장할 때 Jackson vs GenericJackson2 등

### 추천 학습 자료
- **영상:** "캐시 설계 전략" 관련 한국어 영상
- **블로그:** "spring boot redis cache aside pattern" 검색
- **실습:** Spring Boot에서 @Cacheable 사용해보고,
  Redis CLI(redis-cli)로 실제 저장된 키-값 확인

---

## 학습 우선순위 요약

| 순서 | 주제 | 시점 | 예상 소요 |
|---|---|---|---|
| 1 | Kafka 기본 개념 + 로컬 실습 | 1주차 전 | 5~7일 |
| 2 | PostgreSQL (MySQL과의 차이) | 1주차 | 2~3일 |
| 3 | MSA 설계 원칙 + Saga 패턴 | 2주차 전 | 3~4일 |
| 4 | Docker 네트워킹 | 2주차 | 1~2일 |
| 5 | OpenFeign + Resilience4j | 3주차 전 | 2~3일 |
| 6 | Redis 캐싱 전략 | 4주차 전 | 2~3일 |
| 7 | Kafka 심화 (Outbox, DLQ) | 6주차 전 | 3~4일 |

> 전체적으로 프로젝트 시작 전 2주 정도를 "학습 기간"으로 잡는 걸 권장합니다.
> 코드를 작성하면서 동시에 배우면 시행착오가 2~3배로 늘어납니다.
> 특히 Kafka와 MSA 개념은 코드 전에 반드시 이해하고 들어가세요.
