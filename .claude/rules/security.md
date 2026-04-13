---
paths:
  - "src/**/security/**"
  - "src/**/config/Security*"
  - "src/**/filter/**"
  - "src/**/auth/**"
---

# 보안 규칙

## JWT 인증

- Access Token: 만료 30분, Header `Authorization: Bearer {token}`
- Refresh Token: 만료 7일, HttpOnly Cookie로 전달
- JWT Secret은 환경변수 `JWT_SECRET`에서 로드. 256bit 이상
- 토큰 페이로드에 민감한 정보(비밀번호, 이메일) 포함 금지. `userId`, `role`만 포함

### JWT 필터 위치

common 모듈의 `JwtAuthenticationFilter`에서 토큰 검증:
- 토큰 유효 → SecurityContext에 인증 정보 저장
- 토큰 만료 → 401 응답
- 토큰 없음 → 인증 불필요 엔드포인트는 통과, 필요 엔드포인트는 401

### 인증 불필요 엔드포인트

```
POST   /api/v1/auth/login
POST   /api/v1/auth/signup
POST   /api/v1/auth/refresh
GET    /api/v1/performances/**    (공연 정보 조회는 비로그인 허용)
GET    /actuator/health
```

그 외 모든 엔드포인트는 인증 필수.

## 소셜 로그인

소셜 로그인 후 `app_user` 테이블에 유저 생성/조회하여 JWT 발급.
신규 유저는 `provider` + `provider_id`로 식별.

### 확정된 제공자 (Provider)
- **Kakao**
  - Client ID: 카카오 개발자 콘솔에서 발급
  - Redirect URI: `https://yourdomain.com/api/v1/auth/kakao/callback` (운영), `http://localhost:8080/api/v1/auth/kakao/callback` (개발)
  - Scope: profile_nickname, account_email
- **Google**
  - Client ID: Google Cloud Console에서 발급
  - Redirect URI: `https://yourdomain.com/api/v1/auth/google/callback` (운영), `http://localhost:8080/api/v1/auth/google/callback` (개발)
  - Scope: profile, email


## 입력값 검증

- 모든 Request DTO에 Bean Validation 적용 (`@NotBlank`, `@Size`, `@Min` 등)
- 경로 변수 `@PathVariable`은 양수 검증: `@Positive`
- SQL Injection 방지: QueryDSL / JPA 파라미터 바인딩만 사용. 문자열 결합 쿼리 금지
- XSS 방지: 사용자 입력 텍스트(메모, 리뷰)는 HTML 태그 이스케이프 처리

## 파일 업로드

- S3 Presigned URL 방식 사용 (서버가 파일을 직접 받지 않음)
- 허용 확장자: jpg, jpeg, png, webp
- 파일 크기 제한: 10MB
- 파일명: UUID로 변환하여 저장 (원본 파일명 노출 방지)
- S3 경로: `archives/{userId}/{UUID}.{확장자}`

## CORS 설정

<!-- ============================================================
  ✏️ 직접 작성 필요: 허용 도메인
  프론트엔드 배포 도메인이 확정되면 아래를 업데이트하세요.
  
  예시:
  - 개발: http://localhost:3000
  - 운영: https://stagediary.com
============================================================ -->

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("허용할 도메인")  // ✏️ 확정 후 수정
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

## 금지 패턴

- 비밀번호 평문 저장 금지. BCryptPasswordEncoder 사용
- API 키를 소스코드에 하드코딩 금지
- 로그에 비밀번호, 토큰 전체값 출력 금지 (마스킹 처리)
- `@CrossOrigin("*")` 컨트롤러 레벨 사용 금지. 전역 CORS 설정 사용
