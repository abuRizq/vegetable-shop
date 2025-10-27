# syntax=docker/dockerfile:1

# ---- Build stage ------------------------------------------------------------
ARG MAVEN_VERSION=3.9.7
ARG JDK_VERSION=17

FROM maven:${MAVEN_VERSION}-eclipse-temurin-${JDK_VERSION} AS build
WORKDIR /workspace

# Copy only the pom first to leverage Docker layer caching
COPY pom.xml .
# Download dependencies to speed up subsequent builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

# Now copy the actual source and build the jar
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests clean package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:${JDK_VERSION}-jre
# Create non-root user for security
RUN adduser --system --group spring
USER spring:spring

WORKDIR /app
# Copy ONLY the Spring Boot fat jar (repackaged) to avoid "no main manifest" error
COPY --from=build /workspace/target/*-exec.jar /app/app.jar

# Render injects PORT, and your app reads it (server.port=${PORT:8080})
EXPOSE 8080

# Sensible JVM defaults for containers
ENV JAVA_OPTS="-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=prod

# If you add Actuator later, you can enable this healthcheck:
# HEALTHCHECK --interval=30s --timeout=3s \
#   CMD wget -qO- http://127.0.0.1:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
