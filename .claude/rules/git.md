# Git 워크플로우

## 브랜치 전략

```
main                          ← 배포 가능한 상태만 유지
├── develop                   ← 개발 통합 브랜치
│   ├── feature/perf/공연-CRUD           ← 기능 개발
│   ├── feature/archive/관람기록-생성     ← 기능 개발
│   ├── feature/noti/FCM-연동           ← 기능 개발
│   ├── fix/perf/검색-NPE-수정          ← 버그 수정
│   └── infra/kafka-설정                ← 인프라 작업
└── hotfix/긴급수정                      ← 프로덕션 긴급 수정
```

### 브랜치 네이밍

`{타입}/{서비스약어}/{설명}` 형식:
- 서비스 약어: `perf` (performance), `archive`, `noti` (notification), `common`
- 타입: `feature`, `fix`, `infra`, `refactor`, `test`, `docs`
- 설명: 한글 허용, 공백 대신 하이픈

예시:
- `feature/perf/KOPIS-API-연동`
- `feature/archive/개인통계-API`
- `fix/noti/FCM-토큰-갱신-오류`
- `infra/CI-CD-파이프라인`

## 커밋 메시지 컨벤션

`{타입}({범위}): {설명}` 형식. 한글 허용:

```
feat(performance): 공연 검색 API 구현
fix(notification): FCM 토큰 만료 시 재등록 로직 추가
refactor(archive): 통계 쿼리 QueryDSL로 전환
test(performance): 공연 서비스 단위 테스트 추가
docs: API 문서 Swagger 설정
chore: Gradle 의존성 업데이트
infra: GitHub Actions 배포 워크플로우 추가
```

### 타입 목록

| 타입 | 설명 |
|---|---|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링 (기능 변경 없음) |
| test | 테스트 추가/수정 |
| docs | 문서 변경 |
| chore | 빌드, 설정 변경 |
| infra | 인프라, CI/CD 변경 |
| style | 코드 포맷팅 (기능 변경 없음) |

## 커밋 규칙

- 커밋 단위: 하나의 논리적 변경 = 하나의 커밋
- 커밋 전 반드시 해당 서비스 테스트 실행: `./gradlew :{서비스}:test`
- WIP(Work In Progress) 커밋 금지. 완결된 단위로만 커밋
- `.env`, 시크릿 키, 민감한 설정 파일 커밋 금지

## .gitignore 필수 항목

```gitignore
# IDE
../../.idea/
*.iml
.vscode/

# Build
build/
out/
*.jar

# Environment
.env
.env.*
!.env.example

# OS
.DS_Store
Thumbs.db

# Claude Code 개인 설정
.claude/CLAUDE.local.md

# 로그
*.log
logs/
```

## PR (Pull Request)

<!-- ============================================================
  ✏️ 직접 작성 필요: PR 템플릿
  실제 프로젝트에서 .github/PULL_REQUEST_TEMPLATE.md로 저장하세요.
  아래는 예시입니다. 팀(혹은 본인) 워크플로우에 맞게 수정하세요.
============================================================ -->

PR 제목: 커밋 메시지와 동일한 형식
PR 본문에 포함할 내용:
- 변경 사항 요약
- 관련 Jira 이슈 번호
- 테스트 결과 스크린샷 (API 응답 등)
- 리뷰 요청 사항

1인 프로젝트이지만, 모든 feature 브랜치는 PR을 거쳐 develop에 머지한다.
면접에서 "Git 워크플로우를 어떻게 관리했냐"는 질문에 PR 기록으로 답할 수 있다.
