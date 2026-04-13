# 공통 코드 컨벤션

## 패키지 구조 (모든 서비스 동일)

```
com.stagediary.{서비스명}/
├── controller/        # REST API 엔드포인트
├── service/           # 비즈니스 로직
├── repository/        # JPA Repository + QueryDSL
├── domain/            # 엔티티, 열거형(Enum)
├── dto/
│   ├── request/       # 요청 DTO
│   └── response/      # 응답 DTO
├── config/            # 설정 클래스 (@Configuration)
├── exception/         # 커스텀 예외
├── kafka/
│   ├── producer/      # Kafka 프로듀서
│   ├── consumer/      # Kafka 컨슈머
│   └── event/         # 이벤트 DTO
└── util/              # 유틸리티
```

## 네이밍 규칙

- 클래스: PascalCase. 접미사로 역할 표시 (`PerformanceService`, `ArchiveController`)
- 메서드: camelCase. 동사로 시작 (`findByGenre`, `createArchive`)
- 상수: UPPER_SNAKE_CASE (`MAX_PHOTO_COUNT`)
- 패키지: 소문자 단일 단어 (`controller`, `service`, `repository`)
- DTO: 용도 + Request/Response (`PerformanceSearchRequest`, `ArchiveDetailResponse`)
- Enum: PascalCase, 값은 UPPER_SNAKE_CASE (`Genre.MUSICAL`, `AlarmType.TICKETING_D1`)

## API 응답 포맷

모든 컨트롤러는 `ApiResponse<T>`를 반환한다:

```java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message
) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
}
```

## 예외 처리

- 커스텀 예외는 `BusinessException`을 상속
- `@RestControllerAdvice`의 `GlobalExceptionHandler`에서 일괄 처리
- HTTP 상태코드: 400 (잘못된 요청), 401 (인증 실패), 403 (권한 없음), 404 (리소스 없음), 500 (서버 오류)
- 예외 응답도 `ApiResponse` 형식 유지

## 로깅

- 로그 레벨: Controller → INFO, Service → DEBUG, Repository → TRACE
- 외부 API 호출(KOPIS, FCM)은 요청/응답 모두 INFO 레벨로 기록
- 예외 발생 시 ERROR + 스택트레이스
- MDC에 `requestId`를 넣어 요청 추적 가능하게 구성

## 금지 패턴

- `@Autowired` 필드 주입 금지. 생성자 주입만 사용 (`@RequiredArgsConstructor`)
- `Optional.get()` 직접 호출 금지. `orElseThrow()`로 명시적 예외 던지기
- 엔티티를 API 응답으로 직접 반환 금지. 반드시 DTO로 변환
- `System.out.println` 금지. Slf4j 로거 사용
- 하드코딩된 문자열/숫자 금지. 상수 또는 설정값으로 분리
- `@Transactional`을 Controller에 사용 금지. Service 레이어에서만 사용
- `*` import 금지. 명시적 import만 사용
