# syntax=docker/dockerfile:1.7
#
# Flyway migrator image
# - Packages SQL migrations from modules/migrations into /flyway/sql.
# - Uses official Flyway image with built-in PostgreSQL driver.
# - Configure with env vars: FLYWAY_URL, FLYWAY_USER, FLYWAY_PASSWORD, etc.
#
FROM flyway/flyway:10.17-alpine

# Copy versioned migrations
# Project path: backend/modules/migrations/src/main/resources/db/migration/
ARG MIGRATIONS_SRC=backend/modules/migrations/src/main/resources/db/migration
COPY ${MIGRATIONS_SRC} /flyway/sql

# Optionally copy Flyway configuration (uncomment if you keep a flyway.conf)
# COPY backend/modules/migrations/flyway.conf /flyway/conf/flyway.conf

# Default command waits/retries and runs migrate.
# Configure at runtime:
#   docker run --rm -e FLYWAY_URL=jdbc:postgresql://db:5432/veggieshop \
#                -e FLYWAY_USER=app -e FLYWAY_PASSWORD=secret <image>
ENV FLYWAY_CONNECT_RETRIES=60
CMD ["-connectRetries=60", "migrate"]
