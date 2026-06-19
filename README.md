# algo-review
알고리즘 문제 풀이 이력과 오답 복습을 관리하는 개인 학습 트래커

## 기술 스택

- 웹: React + TypeScript + Vite
- API: Spring Boot + Gradle
- 인프라: Docker Compose + PostgreSQL

## 프로젝트 구조

```text
apps/
  api/   Spring Boot API
  web/   React 웹 앱
infra/   Docker Compose 및 로컬 환경 파일
```

## 로컬 실행

```bash
cd infra
docker compose up --build
```

환경값을 바꾸려면 `infra/.env.example`을 `infra/.env`로 복사해서 수정하면 됩니다.

- 웹: http://localhost:3000
- API 상태 확인: http://localhost:8080/healthz

## 개발 명령

```bash
./gradlew :apps:api:test
cd apps/web && npm ci && npm run dev
```
