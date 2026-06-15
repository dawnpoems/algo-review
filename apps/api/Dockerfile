FROM gradle:8.14-jdk17-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle .
COPY apps/api/build.gradle ./apps/api/
COPY apps/api/src ./apps/api/src

RUN chmod +x ./gradlew && ./gradlew :apps:api:build -x test

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/apps/api/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
