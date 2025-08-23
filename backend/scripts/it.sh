#!/usr/bin/env bash
# it.sh — Integration/System tests runner (Testcontainers/Failsafe)
#
# Executes only the system tests module using Maven Failsafe.
# Can optionally bring up/down local infra via docker-compose or custom scripts.
#
# Usage:
#   ./scripts/it.sh [options]
#
# Options:
#   --profiles <csv>     Profiles to use (default: ci)
#   --pattern <glob>     Only run ITs matching pattern (maps to -Dit.test=...)
#   --threads <N|1C>     Maven parallelism
#   --settings <path>    Custom Maven settings.xml
#   --compose <path>     docker-compose file to bring up (default: config/local/docker-compose.yml)
#   --.env-file <path>    ..env file to pass to docker-compose (default: backend/..env if exists)
#   --up                 Start compose before tests (or call scripts/dev_up.sh)
#   --down               Stop compose after tests (or call scripts/dev_down.sh)
#   --debug              Print executed commands
#   -h, --help           Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

PROFILES="ci"
PATTERN=""
THREADS=""
SETTINGS=""
COMPOSE_FILE="${BACKEND_DIR}/config/local/docker-compose.yml"
ENV_FILE="${BACKEND_DIR}/.env"
DO_UP=0
DO_DOWN=0
DEBUG=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profiles) PROFILES="$2"; shift 2;;
    --pattern)  PATTERN="$2"; shift 2;;
    --threads)  THREADS="$2"; shift 2;;
    --settings) SETTINGS="$2"; shift 2;;
    --compose)  COMPOSE_FILE="$2"; shift 2;;
    --.env-file) ENV_FILE="$2"; shift 2;;
    --up)       DO_UP=1; shift;;
    --down)     DO_DOWN=1; shift;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

start_infra() {
  if [[ -x "${BACKEND_DIR}/scripts/dev_up.sh" ]]; then
    echo "[it] Starting local infra via scripts/dev_up.sh ..."
    "${BACKEND_DIR}/scripts/dev_up.sh"
  elif [[ -f "${COMPOSE_FILE}" ]]; then
    echo "[it] Starting docker-compose: ${COMPOSE_FILE}"
    if [[ -f "${ENV_FILE}" ]]; then ENV_OPT=(--.env-file "${ENV_FILE}"); else ENV_OPT=(); fi
    docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" up -d
  else
    echo "[it] No infra script or compose file found; skipping infra startup."
  fi
}

stop_infra() {
  if [[ -x "${BACKEND_DIR}/scripts/dev_down.sh" ]]; then
    echo "[it] Stopping local infra via scripts/dev_down.sh ..."
    "${BACKEND_DIR}/scripts/dev_down.sh"
  elif [[ -f "${COMPOSE_FILE}" ]]; then
    echo "[it] Stopping docker-compose: ${COMPOSE_FILE}"
    if [[ -f "${ENV_FILE}" ]]; then ENV_OPT=(--.env-file "${ENV_FILE}"); else ENV_OPT=(); fi
    docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" down -v
  else
    echo "[it] No infra script or compose file found; skipping infra teardown."
  fi
}

MVNW="${BACKEND_DIR}/mvnw"
MVN_BIN=()
if [[ -x "${MVNW}" ]]; then MVN_BIN+=("${MVNW}"); else MVN_BIN+=("mvn"); fi

if [[ -n "${SETTINGS}" ]]; then
  MVN_BIN+=(-s "${SETTINGS}")
elif [[ -f "${BACKEND_DIR}/ci/maven-settings.xml" ]]; then
  MVN_BIN+=(-s "${BACKEND_DIR}/ci/maven-settings.xml")
fi

MVN_BIN+=(-B -V -U -Dstyle.color=always)

ARGS=(verify -P "${PROFILES}" -DskipUTs=true -pl modules/system-tests -am)
if [[ -n "${PATTERN}" ]]; then ARGS+=(-Dit.test="${PATTERN}"); fi
if [[ -n "${THREADS}" ]]; then ARGS+=(-T "${THREADS}"); fi

[[ "${DO_UP}" -eq 1 ]] && start_infra

echo "[it] Running system tests with: ${MVN_BIN[*]} ${ARGS[*]}"
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS[@]}" )

[[ "${DO_DOWN}" -eq 1 ]] && stop_infra

echo "[it] Done."
