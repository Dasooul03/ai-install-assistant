# 多阶段构建 — Gradle 编译 + JRE 运行
FROM gradle:8.12-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY src/ src/
RUN gradle build --no-daemon -x test -x check

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
