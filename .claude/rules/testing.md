---
paths:
  - "src/**/test/**"
  - "src/**/Test*.java"
  - "src/**/*Test.java"
  - "src/**/*Tests.java"
---

# 테스트 규칙

## 테스트 분류

| 종류 | 도구 | 대상 | 실행 속도 |
|---|---|---|---|
| 단위 테스트 | JUnit 5 + Mockito | Service, Util | 빠름 |
| 리포지토리 테스트 | @DataJpaTest | Repository, QueryDSL | 중간 |
| 통합 테스트 | @SpringBootTest + Testcontainers | API 전체 흐름 | 느림 |
| Kafka 테스트 | @EmbeddedKafka | 프로듀서/컨슈머 | 중간 |

## 테스트 메서드 네이밍

`should_결과_when_조건` 패턴:

```java
@Test
void should_ReturnPerformance_when_ValidIdGiven() { ... }

@Test
void should_ThrowException_when_PerformanceNotFound() { ... }

@Test
void should_CreateArchive_when_ValidRequest() { ... }
```

## 단위 테스트 (Service)

- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`으로 의존성 주입, `@InjectMocks`로 테스트 대상 생성
- `@MockBean` 사용 금지 (Spring 컨텍스트 로딩 불필요)
- Given-When-Then 구조 필수. 주석으로 구분

```java
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private PerformanceService performanceService;

    @Test
    void should_ReturnPerformance_when_ValidIdGiven() {
        // Given
        Performance performance = Performance.builder().title("레미제라블").genre(Genre.MUSICAL).build();
        given(performanceRepository.findById(1L)).willReturn(Optional.of(performance));

        // When
        PerformanceResponse result = performanceService.findById(1L);

        // Then
        assertThat(result.title()).isEqualTo("레미제라블");
    }
}
```

## 리포지토리 테스트

- `@DataJpaTest` + `@Import(QueryDslConfig.class)` 사용
- `@AutoConfigureTestDatabase(replace = Replace.NONE)` + Testcontainers로 실제 PostgreSQL 사용
- 테스트 데이터는 `@BeforeEach`에서 직접 save. SQL 파일 주입 지양

## 통합 테스트

- `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` 사용
- Testcontainers로 PostgreSQL, Redis 실행
- API 호출: `TestRestTemplate` 또는 `MockMvc`
- 인증이 필요한 API: 테스트용 JWT 토큰 생성 유틸 사용

## Kafka 테스트

- `@EmbeddedKafka` 사용하여 인메모리 Kafka로 테스트
- 프로듀서 테스트: 메시지 발행 후 `ConsumerRecord`로 검증
- 컨슈머 테스트: 메시지 전송 후 서비스 호출 여부 검증

## 테스트 커버리지

- 최소 목표: Service 레이어 80% 이상
- Repository (QueryDSL 커스텀 메서드): 필수 테스트
- Controller: 주요 API 경로별 통합 테스트 1개 이상
- 커버리지 측정: JaCoCo 사용

## 금지 패턴

- `@SpringBootTest`를 단위 테스트에 사용 금지 (느려짐)
- 테스트 간 순서 의존성 금지 (각 테스트는 독립적이어야 함)
- `Thread.sleep()`으로 비동기 결과 대기 금지. `Awaitility` 라이브러리 사용
- 테스트 코드에서 `System.out.println` 사용 금지. assertion으로 검증
