---
paths:
  - "src/**/*.java"
---

# Spring Boot 코드 규칙

## 엔티티

- `@Entity` 클래스에는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder` 적용
- `@Id`는 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 사용 (PostgreSQL BIGSERIAL 대응)
- Auditing 필드(`createdAt`, `updatedAt`)는 `@MappedSuperclass`인 `BaseTimeEntity`에 정의
- 연관관계 매핑 시 `FetchType.LAZY` 기본. EAGER 사용 시 반드시 주석으로 이유 명시
- 양방향 매핑 지양. 단방향 매핑 우선. 양방향이 필요한 경우 연관관계 편의 메서드 필수

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "performance", schema = "performance_schema")
public class Performance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Genre genre;

    @Builder
    private Performance(String title, Genre genre) {
        this.title = title;
        this.genre = genre;
    }
}
```

## DTO 변환

- Entity → Response 변환: Response DTO 안에 정적 팩토리 메서드 `from(Entity)` 정의
- Request → Entity 변환: Request DTO 안에 `toEntity()` 메서드 정의

```java
public record PerformanceResponse(Long id, String title, String genre) {
    public static PerformanceResponse from(Performance entity) {
        return new PerformanceResponse(entity.getId(), entity.getTitle(), entity.getGenre().name());
    }
}
```

## 서비스 레이어

- 읽기 전용 메서드: `@Transactional(readOnly = true)`
- 쓰기 메서드: `@Transactional`
- 서비스 클래스 전체에 `@Transactional(readOnly = true)` 걸고, 쓰기 메서드만 `@Transactional` 오버라이드
- 한 메서드의 길이가 30줄을 넘으면 private 메서드로 분리

## 컨트롤러

- 반환 타입: `ResponseEntity<ApiResponse<T>>`
- 경로: `/api/v1/{리소스명}` (복수형)
- 페이지네이션 응답: `SliceResponse<T>` (Cursor 기반)
- 요청 유효성 검증: `@Valid` + Bean Validation 어노테이션 사용
- 컨트롤러에 비즈니스 로직 작성 금지. 서비스 호출만 수행

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PerformanceResponse>> getPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(performanceService.findById(id)));
    }
}
```

## 설정

- 환경별 설정 파일: `application.yml`, `application-local.yml`, `application-prod.yml`
- 민감한 값(DB 비밀번호, API 키)은 환경변수로 주입. yml에 하드코딩 금지
- `@ConfigurationProperties`로 커스텀 설정값 바인딩 (prefix 기반)
