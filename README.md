# algo-review
알고리즘 문제 풀이 이력과 오답 복습을 관리하는 개인 학습 트래커

## Stack

- Web: React + TypeScript + Vite
- API: Spring Boot + Gradle
- Infra: Docker Compose + PostgreSQL

## Structure

```text
apps/
  api/   Spring Boot API
  web/   React web app
infra/   Docker Compose and local environment files
```

## Local Run

```bash
cd infra
docker compose up --build
```

환경값을 바꾸려면 `infra/.env.example`을 `infra/.env`로 복사해서 수정하면 됩니다.

- Web: http://localhost:3000
- API health: http://localhost:8080/healthz

## Development

```bash
./gradlew :apps:api:test
cd apps/web && npm ci && npm run dev
```
