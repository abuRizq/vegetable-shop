#!/usr/bin/env bash
# build.sh — Opinionated Maven build wrapper for Vegetable Shop Backend
#
# Features:
# - Auto-detects Maven Wrapper and repo layout
# - Uses backend/ci/maven-settings.xml if present
# - Handy flags: --fast/--no-tests, --module, --threads, --profiles, --extra
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

JAVA_VERSION_DEFAULT="21"

usage() {
  cat <<'USAGE'
build.sh — build the project with Maven

Usage:
  ./scripts/build.sh [options]

Options:
  --fast                 Build with tests skipped (alias for --no-tests)
  --no-tests             Skip all tests (unit + integration)
  --module <selector>    Build a specific module (e.g., modules/platform or GAV)
  --threads <N|1C>       Maven parallelism (e.g., 1C, 2C, 4, etc.)
  --profiles <csv>       Comma-separated profiles (default: ci)
  --settings <path>      Custom Maven settings.xml path (default: backend/ci/maven-settings.xml if exists)
  --extra "<args>"       Extra arguments passed as-is to Maven
  --debug                Print executed commands (set -x)
  -h, --help             Show this help

Examples:
  ./scripts/build.sh --fast
  ./scripts/build.sh --module apps/veggieshop-service --threads 1C
  ./scripts/build.sh --profiles "ci,release" --extra "-DskipITs=true"
USAGE
}

# Defaults
FAST=0
NO_TESTS=0
MODULE=""
THREADS=""
PROFILES="ci"
SETTINGS=""
EXTRA=""
DEBUG=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --fast) FAST=1; shift ;;
    --no-tests) NO_TESTS=1; shift ;;
    --module) MODULE="${2:-}"; shift 2 ;;
    --threads) THREADS="${2:-}"; shift 2 ;;
    --profiles) PROFILES="${2:-}"; shift 2 ;;
    --settings) SETTINGS="${2:-}"; shift 2 ;;
    --extra) EXTRA="${2:-}"; shift 2 ;;
    --debug) DEBUG=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

MVNW="${BACKEND_DIR}/mvnw"
MVN_BIN=()
if [[ -x "${MVNW}" ]]; then
  MVN_BIN+=("${MVNW}")
else
  MVN_BIN+=("mvn")
fi

if [[ -n "${SETTINGS}" ]]; then
  MVN_BIN+=(-s "${SETTINGS}")
elif [[ -f "${BACKEND_DIR}/ci/maven-settings.xml" ]]; then
  MVN_BIN+=(-s "${BACKEND_DIR}/ci/maven-settings.xml")
fi

MVN_BIN+=(-B -V -U -Dstyle.color=always)

ARGS=(clean package -P "${PROFILES}")

if [[ "${NO_TESTS}" -eq 1 || "${FAST}" -eq 1 ]]; then
  ARGS+=(-DskipTests=true)
fi

if [[ -n "${THREADS}" ]]; then
  ARGS+=(-T "${THREADS}")
fi

if [[ -n "${MODULE}" ]]; then
  ARGS+=(-pl "${MODULE}" -am)
fi

if [[ -n "${EXTRA}" ]]; then
  # shellcheck disable=SC2206
  EXTRA_ARR=(${EXTRA})
  ARGS+=("${EXTRA_ARR[@]}")
fi

echo "[build] Using backend dir: ${BACKEND_DIR}"
echo "[build] Java:"
java -version || echo "Java not found on PATH" >&2

echo "[build] Command: ${MVN_BIN[*]} ${ARGS[*]}"
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS[@]}" )
