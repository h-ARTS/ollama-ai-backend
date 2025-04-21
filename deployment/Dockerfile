FROM gradle:8.12-jdk17 AS builder

# Enable Gradle build cache
ENV GRADLE_USER_HOME=/home/gradle/.gradle

COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /app/
COPY --chown=gradle:gradle gradle /app/gradle

WORKDIR /app

RUN gradle dependencies --no-daemon

COPY --chown=gradle:gradle . /app

RUN gradle bootJar --no-daemon

FROM openjdk:17-jdk-slim

VOLUME /tmp
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
