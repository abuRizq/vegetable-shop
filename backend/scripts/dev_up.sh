#!/usr/bin/env bash
# dev_up.sh — Start local development infrastructure with Docker Compose
#
# Brings up services defined in config/local/docker-compose.yml, then waits for
# common dependencies to be ready (Postgres, Kafka, Redis, MinIO, etc.).
#
# Usage:
#   ./scripts/dev_up.sh [options] [service ...]
#
# Options:
#   --compose <path>     Compose file path (default: backend/config/local/docker-compose.yml)
#   --.env-file <path>    ..env file path (default: backend/..env if exists)
#   --pull               docker compose pull before up
#   --build              docker compose build before up
#   --no-wait            Do not wait for services
#   --timeout <sec>      Wait timeout per dependency (default: 60)
#   --debug              Print executed commands
#   -h, --help           Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

COMPOSE_FILE="${BACKEND_DIR}/config/local/docker-compose.yml"
ENV_FILE="${BACKEND_DIR}/.env"
DO_PULL=0
DO_BUILD=0
NO_WAIT=0
TIMEOUT=60
DEBUG=0

SERVICES=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose)  COMPOSE_FILE="$2"; shift 2;;
    --.env-file) ENV_FILE="$2"; shift 2;;
    --pull)     DO_PULL=1; shift;;
    --build)    DO_BUILD=1; shift;;
    --no-wait)  NO_WAIT=1; shift;;
    --timeout)  TIMEOUT="$2"; shift 2;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) SERVICES+=("$1"); shift;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "[dev_up] Compose file not found: ${COMPOSE_FILE}" >&2
  exit 1
fi

ENV_OPT=()
if [[ -f "${ENV_FILE}" ]]; then
  ENV_OPT=(--.env-file "${ENV_FILE}")
  echo "[dev_up] Using env file: ${ENV_FILE}"
fi

if (( DO_PULL==1 )); then
  echo "[dev_up] docker compose pull ..."
  docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" pull "${SERVICES[@]}" || true
fi

if (( DO_BUILD==1 )); then
  echo "[dev_up] docker compose build ..."
  docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" build "${SERVICES[@]}"
fi

echo "[dev_up] docker compose up -d ..."
docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" up -d "${SERVICES[@]}"

if (( NO_WAIT==1 )); then
  echo "[dev_up] Skipping wait checks."
  exit 0
fi

# Utility to call wait-for-it
wait_for() {
  local host="$1"; local port="$2"; local name="${3:-${host}:${port}}"
  if [[ -x "${SCRIPT_DIR}/wait-for-it.sh" ]]; then
    echo "[dev_up] Waiting for ${name} (${host}:${port})"
    "${SCRIPT_DIR}/wait-for-it.sh" "${host}:${port}" -t "${TIMEOUT}" --strict
  else
    echo "[dev_up] wait-for-it.sh not found; skipping wait for ${name}"
  fi
}

# Load .env to capture overrides (non-fatal)
if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC2162
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    if [[ "$line" =~ ^[A-Za-z_][A-Za-z0-9_]*= ]]; then
      export "$line"
    fi
  done < "${ENV_FILE}"
fi

# Known defaults; override via ..env if set
POSTGRES_HOST="${POSTGRES_HOST:-postgres}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"

KAFKA_HOST="${KAFKA_HOST:-kafka}"
KAFKA_PORT="${KAFKA_PORT:-9092}"

REDIS_HOST="${REDIS_HOST:-redis}"
REDIS_PORT="${REDIS_PORT:-6379}"

MINIO_HOST="${MINIO_HOST:-minio}"
MINIO_PORT="${MINIO_PORT:-9000}"

# Wait for typical services if likely present
wait_for "${POSTGRES_HOST}" "${POSTGRES_PORT}" "Postgres"
wait_for "${KAFKA_HOST}" "${KAFKA_PORT}" "Kafka"
wait_for "${REDIS_HOST}" "${REDIS_PORT}" "Redis"
wait_for "${MINIO_HOST}" "${MINIO_PORT}" "MinIO"

echo "[dev_up] All done. Services should be ready."
