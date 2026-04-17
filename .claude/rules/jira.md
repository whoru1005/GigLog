# Jira 프로젝트 관리 규칙

## 프로젝트 설정

- 프로젝트 키: `GL` (StageDiary)
- 보드: 스크럼 보드
- 스프린트: 1스프린트 = 1주, 총 8스프린트
- 스프린트별 상세 계획: `docs/sprint-plan.md` 참고

## 이슈 계층

```
Epic (대기능) → Story (사용자 관점) → Task (개발 작업)
```

## 에픽 목록

| 에픽      | 이름 | 스프린트 |
|---------|---|---|
| GL-EP01 | 프로젝트 기반 구축 | 1~2 |
| GL-EP02 | 공연 정보 서비스 | 3~4 |
| GL-EP03 | 관람 기록 서비스 | 5 |
| GL-EP04 | 알림 서비스 | 6 |
| GL-EP05 | 인프라 & CI/CD | 2, 7 |
| GL-EP06 | 테스트 & 안정화 | 7~8 |

## 스토리 작성 형식

```
제목: [서비스] 기능 요약
설명:
  AS A 사용자
  I WANT TO 원하는 기능
  SO THAT 기대 효과

인수 조건:
  - [ ] 조건 1
  - [ ] 조건 2
```

## 태스크 작성 형식

```
제목: [서비스][레이어] 작업 내용
설명: 구현 상세
관련 스토리: GL-XX
```

레이어 표기: `[Domain]`, `[Repository]`, `[Service]`, `[Controller]`, `[Config]`, `[Kafka]`, `[Infra]`

## 레이블

| 레이블 | 용도 |
|---|---|
| `perf-service` | performance-service |
| `archive-service` | archive-service |
| `noti-service` | notification-service |
| `common` | 공통 모듈 |
| `infra` | 인프라, CI/CD |
| `kafka` | Kafka 이벤트 |
| `database` | DB 스키마, 쿼리 |
| `bug` | 버그 수정 |
| `tech-debt` | 기술 부채 |
| `study` | 학습 필요 항목 |

## 워크플로우

```
TODO → IN PROGRESS → IN REVIEW → DONE
                   ↘ BLOCKED (차단 사유 코멘트 필수)
```

- IN PROGRESS 전환 = 브랜치 생성 시점
- IN REVIEW 전환 = PR 생성 시점
- DONE 전환 = 머지 + 테스트 통과 시점

## Jira-Git 연동

- 브랜치: `feature/perf/GL-15-공연-CRUD`
- 커밋: `feat(performance): GL-15 공연 CRUD API 구현`
- PR 제목: `[GL-15] feat(performance): 공연 CRUD API 구현`

## 스프린트 회고 (매주 종료 시)

회고 이슈 생성 (레이블: `retrospective`):
- Keep (잘한 점) / Problem (문제점) / Try (시도할 것)
- 예상 vs 실제 완료 스토리 포인트 기록

## MCP 활용 시 규칙

- 이슈 생성 시 에픽 링크, 레이블, 스프린트 반드시 지정
- 스토리에는 인수 조건 필수
- 태스크에는 관련 스토리 링크 필수
- 완료 이슈에는 결과 코멘트 남기기 (구현 요약, 트러블슈팅)
