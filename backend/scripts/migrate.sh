#!/usr/bin/env bash
# migrate.sh — Run Flyway database migrations for Vegetable Shop Backend
#
# Uses Flyway Maven Plugin in modules/migrations. Supports passing DB params via flags or ..env.
#
# Usage:
#   ./scripts/migrate.sh [options] [goal]
#
# Goals (Flyway):
#   migrate (default), info, repair, clean, validate, baseline
#
# Options:
#   --url <jdbc-url>        JDBC URL (e.g., jdbc:postgresql://localhost:5432/veggieshop)
#   --user <username>       DB user
#   --password <password>   DB password
#   --schemas <csv>         Flyway schemas
#   --locations <csv>       Flyway locations (override if needed)
#   --settings <path>       Custom Maven settings.xml (default: backend/ci/maven-settings.xml if present)
#   --profiles <csv>        Maven profiles (default: db-tools)
#   --threads <N|1C>        Maven parallelism
#   --wait                  Wait for DB readiness before migrating (default if URL is Postgres)
#   --no-wait               Do not wait for DB readiness
#   --.env-file <path>       Load .env vars from file (default: backend/..env if exists)
#   --debug                 Print executed commands
#   -h, --help              Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

GOAL="migrate"
URL="${DB_URL:-}"
USER="${DB_USER:-}"
PASSWORD="${DB_PASSWORD:-}"
SCHEMAS="${DB_SCHEMAS:-}"
LOCATIONS="${FLYWAY_LOCATIONS:-}"
SETTINGS=""
PROFILES="db-tools"
THREADS=""
WAIT=0
ENV_FILE="${BACKEND_DIR}/.env"
DEBUG=0

# last arg may be a goal if matches known goals
if [[ "${1:-}" =~ ^(migrate|info|repair|clean|validate|baseline)$ ]]; then
  GOAL="$1"; shift
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --url) URL="$2"; shift 2;;
    --user) USER="$2"; shift 2;;
    --password) PASSWORD="$2"; shift 2;;
    --schemas) SCHEMAS="$2"; shift 2;;
    --locations) LOCATIONS="$2"; shift 2;;
    --settings) SETTINGS="$2"; shift 2;;
    --profiles) PROFILES="$2"; shift 2;;
    --threads) THREADS="$2"; shift 2;;
    --wait) WAIT=1; shift;;
    --no-wait) WAIT=0; shift;;
    --.env-file) ENV_FILE="$2"; shift 2;;
    --debug) DEBUG=1; shift;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  end
done

[[ "${DEBUG}" -eq 1 ]] && set -x

# Load ..env if present
load_env_file() {
  local file="$1"
  [[ -f "$file" ]] || return 0
  echo "[migrate] Loading env from ${file}"
  # shellcheck disable=SC2162
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    if [[ "$line" =~ ^[A-Za-z_][A-Za-z0-9_]*= ]]; then
      export "$line"
    fi
  done < "$file"
}

load_env_file "${ENV_FILE}"

# Apply defaults from .env if flags missing
URL="${URL:-${JDBC_URL:-${SPRING_DATASOURCE_URL:-}}}"
USER="${USER:-${SPRING_DATASOURCE_USERNAME:-${POSTGRES_USER:-}}}"
PASSWORD="${PASSWORD:-${SPRING_DATASOURCE_PASSWORD:-${POSTGRES_PASSWORD:-}}}"

if [[ -z "${URL}" || -z "${USER}" ]]; then
  echo "[migrate] Missing --url and/or --user. Provide flags or .env values." >&2
  exit 1
fi

# If Postgres URL, extract host:port to wait
should_wait=0
host=""
port="5432"
if [[ "${URL}" =~ ^jdbc:postgresql://([^:/]+)(:([0-9]+))?/ ]]; then
  host="${BASH_REMATCH[1]}"
  if [[ -n "${BASH_REMATCH[3]:-}" ]]; then port="${BASH_REMATCH[3]}"; fi
  should_wait=1
fi

if (( WAIT==1 || (WAIT==0 && should_wait==1) )); then
  if [[ -x "${SCRIPT_DIR}/wait-for-it.sh" ]]; then
    echo "[migrate] Waiting for ${host}:${port} ..."
    "${SCRIPT_DIR}/wait-for-it.sh" "${host}:${port}" -t 60 --strict
  else
    echo "[migrate] wait-for-it.sh not found; proceeding without explicit wait."
  fi
fi

MVNW="${BACKEND_DIR}/mvnw"
MVN_BIN=()
if [[ -x "${MVNW}" ]]; then MVN_BIN+=("${MVNW}"); else MVN_BIN+=("mvn"); fi

if [[ -n "${SETTINGS}" ]]; then
  MVN_BIN+=(-s "${SETTINGS}")
elif [[ -f "${BACKEND_DIR}/ci/maven-settings.xml" ]]; then
  MVN_BIN+=(-s "${BACKEND_DIR}/ci/maven-settings.xml")
fi

MVN_BIN+=(-B -V -U -Dstyle.color=always)

ARGS=(-pl modules/migrations -am -P "${PROFILES}")
if [[ -n "${THREADS}" ]]; then ARGS+=(-T "${THREADS}"); fi

# Flyway properties
FW=(-Dflyway.url="${URL}" -Dflyway.user="${USER}")
if [[ -n "${PASSWORD}" ]]; then FW+=(-Dflyway.password="${PASSWORD}"); fi
if [[ -n "${SCHEMAS}" ]]; then FW+=(-Dflyway.schemas="${SCHEMAS}"); fi
if [[ -n "${LOCATIONS}" ]]; then FW+=(-Dflyway.locations="${LOCATIONS}"); fi

echo "[migrate] Running Flyway goal: ${GOAL}"
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS[@]}" "org.flywaydb:flyway-maven-plugin:${GOAL}" "${FW[@]}" )
