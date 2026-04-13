---
paths:
  - "src/**/repository/**"
  - "src/**/domain/**"
  - "src/main/resources/db/**"
---

# 데이터베이스 규칙

## 스키마 분리

하나의 RDS 인스턴스에서 서비스별 스키마를 분리한다:
- `performance_schema`: 공연, 공연장, 스케줄, 캐스팅
- `archive_schema`: 유저, 관람 기록, 사진
- `notification_schema`: 구독, 알림 스케줄, FCM 토큰, Outbox

각 서비스의 `application.yml`에서 `default_schema`를 지정:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: performance_schema
```

## 테이블 네이밍

- 테이블명: snake_case, 단수형 (`performance`, `archive`, `venue`)
- 컬럼명: snake_case (`show_date`, `created_at`)
- 인덱스명: `idx_{테이블}_{컬럼}` (`idx_archive_user`, `idx_schedule_perf_date`)
- 유니크 제약: `uq_{테이블}_{컬럼}`

## Flyway 마이그레이션

- 파일 위치: `src/main/resources/db/migration/`
- 파일명: `V{버전}__{설명}.sql` (언더스코어 2개)
  - 예: `V1__create_performance_table.sql`
  - 예: `V2__add_venue_table.sql`
- DDL과 DML은 별도 파일로 분리
- 마이그레이션 파일은 한번 적용되면 수정 금지. 변경이 필요하면 새 버전 파일 생성

## QueryDSL

- QueryDSL 클래스는 `{Entity}RepositoryCustom` 인터페이스 + `{Entity}RepositoryImpl` 구현체로 분리
- `JPAQueryFactory`는 Config에서 Bean 등록 후 생성자 주입
- 동적 쿼리 조건은 `BooleanExpression`을 반환하는 private 메서드로 분리
- N+1 방지: 연관 엔티티 조회 시 `fetchJoin()` 필수 적용

```java
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Performance> searchByCondition(PerformanceSearchRequest request, Pageable pageable) {
        List<Performance> results = queryFactory
            .selectFrom(performance)
            .leftJoin(performance.venue, venue).fetchJoin()
            .where(
                genreEq(request.genre()),
                titleContains(request.keyword()),
                statusEq(request.status())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .orderBy(performance.startDate.desc())
            .fetch();

        return SliceUtil.toSlice(results, pageable);
    }

    private BooleanExpression genreEq(Genre genre) {
        return genre != null ? performance.genre.eq(genre) : null;
    }
}
```

## 페이지네이션

- Cursor 기반 페이지네이션 사용 (`Slice<T>`, `limit + 1` 기법)
- Offset 기반은 관리자 페이지 등 데이터 적은 곳에서만 허용
- 커서 값은 정렬 기준 컬럼의 마지막 값을 사용

## 인덱스 전략

- WHERE 조건에 자주 사용되는 컬럼에 인덱스 생성
- Partial Index 적극 활용 (예: `WHERE status = 'PENDING'`)
- 복합 인덱스는 카디널리티 높은 컬럼을 앞에 배치
- 인덱스 추가 시 `EXPLAIN ANALYZE`로 실행계획 확인 필수

## 금지 패턴

- Native Query 사용 금지 (QueryDSL 또는 JPQL 사용)
  - 단, Flyway 마이그레이션 SQL과 벌크 연산은 예외
- `CascadeType.ALL` 금지. 필요한 Cascade만 명시적으로 지정
- `@OneToMany`에서 `orphanRemoval = true` 사용 시 주석으로 의도 명시
- 엔티티에 `@Setter` 사용 금지. 도메인 메서드로 상태 변경
