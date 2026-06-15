.PHONY: check check-web check-api compose-up compose-down

check: check-api check-web

check-web:
	cd apps/web && npm ci && npm run type-check && npm run lint && npm run build

check-api:
	./gradlew :apps:api:test

compose-up:
	cd infra && docker compose up --build

compose-down:
	cd infra && docker compose down
