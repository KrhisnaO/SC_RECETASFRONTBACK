# Multi-stage: usa la imagen oficial de Gradle (evita problemas de CRLF en gradlew desde Windows)
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle settings.gradle build.gradle ./
COPY --chown=gradle:gradle src src
RUN gradle bootJar -x test --no-daemon

# Imagen final liviana
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
