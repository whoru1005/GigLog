---
paths:
  - "src/**/kafka/**"
  - "src/**/event/**"
  - "src/**/config/Kafka*"
---

# Kafka 이벤트 규칙

## 토픽 네이밍

`{도메인}.{엔티티}.{동작}` 형식:
- `performance.schedule.created`
- `performance.schedule.updated`
- `archive.archive.created`
- `subscription.subscription.created`

DLQ 토픽: `{원래토픽}.dlq`
- `performance.schedule.created.dlq`

## 이벤트 페이로드 구조

모든 이벤트는 아래 공통 필드를 포함한다:

```java
public record BaseEvent(
    String eventId,       // UUID. 멱등성 처리에 사용
    String eventType,     // "SCHEDULE_CREATED", "ARCHIVE_CREATED" 등
    Instant timestamp,    // 이벤트 발생 시각
    Object payload        // 도메인별 데이터
) {}
```

페이로드 예시:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "SCHEDULE_CREATED",
  "timestamp": "2026-04-13T10:00:00Z",
  "payload": {
    "performanceId": 1,
    "scheduleId": 10,
    "title": "레미제라블",
    "showDate": "2026-06-15",
    "ticketingOpen": "2026-05-01T11:00:00Z"
  }
}
```

## 프로듀서 규칙

- 직렬화: `JsonSerializer` 사용 (Avro는 사용하지 않음)
- 파티션 키: `performanceId` 사용 (같은 공연 이벤트의 순서 보장)
- 프로듀서는 `KafkaTemplate<String, BaseEvent>` 타입으로 통일
- 이벤트 발행 시 반드시 로그 남기기 (토픽, 키, eventId)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventProducer {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void publishScheduleCreated(ScheduleCreatedPayload payload) {
        BaseEvent event = new BaseEvent(
            UUID.randomUUID().toString(),
            "SCHEDULE_CREATED",
            Instant.now(),
            payload
        );
        kafkaTemplate.send("performance.schedule.created", 
            String.valueOf(payload.performanceId()), event);
        log.info("Published SCHEDULE_CREATED event: eventId={}, performanceId={}", 
            event.eventId(), payload.performanceId());
    }
}
```

## 컨슈머 규칙

- Consumer Group: `{서비스명}-group` (예: `notification-service-group`)
- `@KafkaListener`에 `groupId`, `topics` 명시
- 멱등성 처리: `eventId`를 `processed_events` 테이블에 저장하여 중복 소비 방지
- 처리 실패 시 3회 재시도 → DLQ로 이동
- 컨슈머 메서드는 `try-catch`로 감싸서 예외를 삼키지 않고 로깅

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventConsumer {

    private final NotificationScheduleService scheduleService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "performance.schedule.created", groupId = "notification-service-group")
    public void handleScheduleCreated(BaseEvent event) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            log.warn("Duplicate event skipped: eventId={}", event.eventId());
            return;
        }
        try {
            scheduleService.createNotificationSchedule(event);
            processedEventRepository.save(new ProcessedEvent(event.eventId()));
            log.info("Processed SCHEDULE_CREATED: eventId={}", event.eventId());
        } catch (Exception e) {
            log.error("Failed to process event: eventId={}", event.eventId(), e);
            throw e; // 재시도 트리거
        }
    }
}
```

## Transactional Outbox 패턴

notification-service에서 이벤트 발행 신뢰성 보장을 위해 사용:

1. 비즈니스 로직과 같은 트랜잭션으로 `outbox_event` 테이블에 INSERT
2. 별도 `@Scheduled` 폴링 스케줄러가 PENDING 이벤트를 Kafka로 발행
3. 발행 성공 시 상태를 PUBLISHED로 업데이트
4. 폴링 주기: 5초

```java
@Scheduled(fixedDelay = 5000)
@Transactional
public void publishOutboxEvents() {
    List<OutboxEvent> pending = outboxRepository.findByStatus("PENDING");
    for (OutboxEvent event : pending) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload());
            event.markPublished();
        } catch (Exception e) {
            log.error("Outbox publish failed: id={}", event.getId(), e);
        }
    }
}
```

## DLQ 처리

- DLQ에 쌓인 이벤트는 CloudWatch 알람으로 감지
- 수동 재처리 또는 원인 분석 후 조치
- DLQ 컨슈머는 별도로 만들지 않음 (모니터링 + 수동 처리)

## Kafka 설정

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${spring.application.name}-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.stagediary.*"
```
