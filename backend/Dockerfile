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

# Create non-root user for better security
RUN addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app

# Copy the Spring Boot fat jar (repackaged as *-exec.jar)
COPY --from=build /workspace/target/*-exec.jar /app/app.jar

# Make sure the non-root user owns the app files
RUN chown -R spring:spring /app

USER spring:spring

# Render provides PORT; your app maps it via server.port=${PORT:8080}
EXPOSE 8080

# Sensible JVM defaults for containers (tune as needed)
ENV JAVA_OPTS="-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true" \
    TZ=UTC

# Keep profile selection outside the image (via env on the platform)
# If you need a default, set it in the deployment env:
# ENV SPRING_PROFILES_ACTIVE=prod

# Optional healthcheck (enable when actuator is available)
# HEALTHCHECK --interval=30s --timeout=3s \
#   CMD wget -qO- http://127.0.0.1:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
