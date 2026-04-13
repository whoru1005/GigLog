---
paths:
  - "docker/**"
  - "Dockerfile*"
  - "docker-compose*.yml"
  - ".github/workflows/**"
  - "scripts/**"
---

# 인프라 & 배포 규칙

## Docker

### Dockerfile (모든 서비스 공통 패턴)

멀티스테이지 빌드 사용:

```dockerfile
# Stage 1: Build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY ../../../../Desktop .
RUN gradle :performance-service:bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/performance-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose 로컬 환경

```yaml
# docker/docker-compose-local.yml
# PostgreSQL, Redis, Kafka(+Zookeeper)를 로컬에서 실행
```

서비스 간 통신은 docker-compose 서비스명 사용:
- PostgreSQL: `postgres:5432`
- Redis: `redis:6379`
- Kafka: `kafka:29092` (컨테이너 내부), `localhost:9092` (호스트에서 접근)

### Kafka ADVERTISED_LISTENERS 설정

Docker 환경에서 Kafka 연결 문제의 90%는 이 설정 때문이다:

```yaml
kafka:
  environment:
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
    KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
```

- `kafka:29092` → 같은 Docker 네트워크의 다른 컨테이너가 접근
- `localhost:9092` → 호스트 머신(IDE에서 직접 실행)이 접근

## CI/CD (GitHub Actions)

### 워크플로우 구조

서비스별 독립 워크플로우. 해당 서비스 디렉토리 변경 시에만 트리거:

```yaml
# .github/workflows/deploy-performance.yml
name: Deploy Performance Service

on:
  push:
    branches: [main]
    paths:
      - 'performance-service/**'
      - 'common/**'

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build
        run: ./gradlew :performance-service:build
      - name: Docker Build & Push
        run: |
          docker build -t ${{ secrets.DOCKER_USERNAME }}/performance-service:latest -f performance-service/Dockerfile .
          docker push ${{ secrets.DOCKER_USERNAME }}/performance-service:latest
      - name: Deploy to EC2
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ubuntu
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            docker pull ${{ secrets.DOCKER_USERNAME }}/performance-service:latest
            docker-compose -f docker-compose-prod.yml up -d performance-service
```

### CI/CD 규칙

- `common/` 모듈 변경 시 모든 서비스 워크플로우 트리거
- 테스트 실패 시 배포 차단 (build 단계에서 test 포함)
- Docker 이미지 태그: `latest` + 커밋 SHA (`abc1234`)
- Secrets는 GitHub Repository Settings에서 관리. 워크플로우 파일에 하드코딩 금지

## AWS 인프라

### 리소스 네이밍

`stagediary-{환경}-{리소스}` 형식:
- `stagediary-prod-alb`
- `stagediary-prod-rds`
- `stagediary-prod-ec2-performance`

### 보안 그룹 규칙

- ALB: 80, 443 인바운드 허용 (0.0.0.0/0)
- EC2: ALB 보안그룹에서만 8080 인바운드 허용
- RDS: EC2 보안그룹에서만 5432 인바운드 허용
- Redis: EC2 보안그룹에서만 6379 인바운드 허용
- SSH(22): 본인 IP에서만 허용

### 환경변수 관리

EC2에서 환경변수는 `.env` 파일로 관리:

```bash
# /home/ubuntu/.env (절대 Git에 커밋하지 않음)
DB_HOST=stagediary-prod-rds.xxxxxx.ap-northeast-2.rds.amazonaws.com
DB_PASSWORD=xxxxxxxx
REDIS_HOST=stagediary-prod-redis.xxxxxx.cache.amazonaws.com
KAFKA_BOOTSTRAP_SERVERS=172.31.x.x:9092
JWT_SECRET=xxxxxxxx
FCM_PROJECT_ID=xxxxxxxx
```

docker-compose에서 `env_file: .env`로 로드.

## 모니터링

- CloudWatch Agent: EC2 메모리/디스크 사용률 수집
- CloudWatch Logs: Docker 컨테이너 로그 → CloudWatch Log Group으로 전송
- CloudWatch Alarms:
  - EC2 CPU > 80% 지속 5분 → SNS 이메일 알림
  - RDS 커넥션 수 > 80% → SNS 이메일 알림
  - DLQ 토픽 메시지 수 > 0 → SNS 이메일 알림
- 헬스체크: ALB Target Group에서 `/actuator/health` 주기 확인
