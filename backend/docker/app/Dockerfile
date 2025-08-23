    # syntax=docker/dockerfile:1.7
    ############################################################
    # Vegetable Shop - App Dockerfile
    # Best practices:
    #  - Multi-stage build (Maven -> Distroless runtime)
    #  - Cache Maven repository via cache mounts
    #  - Run as non-root
    #  - Minimal runtime image (distroless by default)
    #  - OCI labels for provenance
    ############################################################

    ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-21
    ARG RUNTIME_IMAGE=gcr.io/distroless/java21-debian12

    ########################
    # 1) Build stage
    ########################
    FROM ${MAVEN_IMAGE} AS build
    WORKDIR /workspace

    # Copy everything (the .dockerignore keeps the context lean)
    COPY . /workspace

    # Package the app (skip tests for faster local builds; enable in CI)
    RUN --mount=type=cache,target=/root/.m2 \
        mvn -q -DskipTests -pl apps/veggieshop-service -am clean package

    # Collect the resulting Spring Boot fat JAR
    RUN bash -lc 'set -euo pipefail; JAR=$(ls -1 apps/veggieshop-service/target/*.jar | head -n1); \
        install -D "$JAR" /out/app.jar && echo "Using JAR: $JAR"'

    ########################
    # 2) Runtime stage
    ########################
    FROM ${RUNTIME_IMAGE} AS runtime

    # Distroless provides the "nonroot" user (UID/GID 65532)
    USER 65532:65532

    WORKDIR /app
    COPY --from=build /out/app.jar /app/app.jar

    EXPOSE 8080

    # Reasonable JVM defaults for containers
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 -XX:+HeapDumpOnOutOfMemoryError -Duser.timezone=UTC"
    ENV SPRING_PROFILES_ACTIVE="prod"
    ENV SERVER_PORT="8080"

    # OCI labels
    ARG APP_NAME="veggieshop-service"
    ARG APP_VERSION="0.0.0"
    ARG APP_REVISION="unknown"
    LABEL org.opencontainers.image.title="${APP_NAME}" \
          org.opencontainers.image.version="${APP_VERSION}" \
          org.opencontainers.image.revision="${APP_REVISION}" \
          org.opencontainers.image.description="Vegetable Shop Backend service image (Spring Boot)" \
          org.opencontainers.image.source="https://example.com/vegetable-shop" \
          org.opencontainers.image.vendor="Vegetable Shop" \
          org.opencontainers.image.licenses="Apache-2.0"

    # Note: Distroless has no shell. Prefer health probes in Compose/K8s.
    ENTRYPOINT ["java","-jar","/app/app.jar"]

    ############################################################
    # Optional runtime (switch ARG RUNTIME_IMAGE):
    #  - eclipse-temurin:21-jre-alpine (has shell/curl for in-image HEALTHCHECK)
    # Example (if using Alpine):
    #
    #   RUN apk add --no-cache curl
    #   HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=5 \
#     CMD curl -fsS http://127.0.0.1:${SERVER_PORT}/actuator/health | grep -q "UP" || exit 1
    ############################################################
