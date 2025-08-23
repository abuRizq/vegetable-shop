#!/usr/bin/env bash
# test.sh — Run unit tests and/or system tests consistently
#
# Modes:
#   unit      -> run unit tests only (Surefire), excludes modules/system-tests
#   system    -> run system tests only (Failsafe) in modules/system-tests
#   all       -> run unit then system tests (default)
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() {
  cat <<'USAGE'
test.sh — run project tests

Usage:
  ./scripts/test.sh [mode] [options]

Modes:
  unit      Run unit tests only (default if mode omitted is 'all')
  system    Run system tests only (Testcontainers/Failsafe)
  all       Run unit then system tests

Options:
  --profiles <csv>     Profiles to use (default: pr-checks for unit, ci for system)
  --pattern <glob>     Run only tests matching pattern (e.g., *ServiceTest)
  --threads <N|1C>     Maven parallelism
  --settings <path>    Custom Maven settings.xml
  --debug              Print executed commands (set -x)
  -h, --help           Show help

Examples:
  ./scripts/test.sh unit --threads 1C
  ./scripts/test.sh system --pattern '*IT' --profiles ci
  ./scripts/test.sh all
USAGE
}

MODE="all"
UNIT_PROFILES="pr-checks"
SYSTEM_PROFILES="ci"
PATTERN=""
THREADS=""
SETTINGS=""
DEBUG=0

if [[ "${1:-}" =~ ^(unit|system|all)$ ]]; then
  MODE="$1"; shift
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profiles) UNIT_PROFILES="$2"; SYSTEM_PROFILES="$2"; shift 2 ;;
    --pattern) PATTERN="$2"; shift 2 ;;
    --threads) THREADS="$2"; shift 2 ;;
    --settings) SETTINGS="$2"; shift 2 ;;
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

run_unit() {
  local args=(clean verify -P "${UNIT_PROFILES}" -DskipITs=true -pl '!modules/system-tests' -am)
  if [[ -n "${PATTERN}" ]]; then
    args+=(-Dtest="${PATTERN}")
  fi
  if [[ -n "${THREADS}" ]]; then
    args+=(-T "${THREADS}")
  fi
  echo "[test][unit] ${MVN_BIN[*]} ${args[*]}"
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${args[@]}" )
}

run_system() {
  local args=(verify -P "${SYSTEM_PROFILES}" -DskipUTs=true -pl modules/system-tests -am)
  if [[ -n "${PATTERN}" ]]; then
    args+=(-Dit.test="${PATTERN}")
  fi
  if [[ -n "${THREADS}" ]]; then
    args+=(-T "${THREADS}")
  fi
  echo "[test][system] ${MVN_BIN[*]} ${args[*]}"
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${args[@]}" )
}

case "${MODE}" in
  unit) run_unit ;;
  system) run_system ;;
  all) run_unit && run_system ;;
esac
