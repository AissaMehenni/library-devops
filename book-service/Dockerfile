# syntax=docker/dockerfile:1.6
# Multi-stage build inspired by https://github.com/charroux/rent/blob/main/MyService/Dockerfile

# ---- Build stage ----
FROM gradle:8.10-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle settings.gradle build.gradle ./
COPY --chown=gradle:gradle src ./src

RUN gradle clean bootJar --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8081
ENV SERVER_PORT=8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
