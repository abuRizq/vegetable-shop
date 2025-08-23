#!/usr/bin/env bash
# lint.sh — Static checks & style validation for Vegetable Shop Backend
#
# Runs project-wide static analysis configured in the Maven build.
# By default, executes the PR-checks profile (Checkstyle/PMD/SpotBugs/etc.).
# Optional modes allow applying formatters (e.g., Spotless) when available.
#
# Usage:
#   ./scripts/lint.sh [check|fix] [options]
#
# Modes:
#   check (default)  Run static analysis and fail on findings.
#   fix              Apply auto-fixes (e.g., Spotless apply) then run checks.
#
# Options:
#   --module <selector>  Run against a specific module (e.g., modules/platform or GAV)
#   --threads <N|1C>     Maven parallelism (e.g., 1C, 2C, 4)
#   --settings <path>    Custom Maven settings.xml (default: backend/ci/maven-settings.xml if present)
#   --extra "<args>"     Extra Maven arguments passed as-is
#   --debug              Print executed commands
#   -h, --help           Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,40p' "$0"; }

MODE="check"
MODULE=""
THREADS=""
SETTINGS=""
EXTRA=""
DEBUG=0

if [[ "${1:-}" =~ ^(check|fix)$ ]]; then MODE="$1"; shift; fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --module)   MODULE="${2:-}"; shift 2;;
    --threads)  THREADS="${2:-}"; shift 2;;
    --settings) SETTINGS="${2:-}"; shift 2;;
    --extra)    EXTRA="${2:-}"; shift 2;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

MVNW="${BACKEND_DIR}/mvnw"
MVN_BIN=()
if [[ -x "${MVNW}" ]]; then MVN_BIN+=("${MVNW}"); else MVN_BIN+=("mvn"); fi

if [[ -n "${SETTINGS}" ]]; then
  MVN_BIN+=(-s "${SETTINGS}")
elif [[ -f "${BACKEND_DIR}/ci/maven-settings.xml" ]]; then
  MVN_BIN+=(-s "${BACKEND_DIR}/ci/maven-settings.xml")
fi

MVN_BIN+=(-B -V -U -Dstyle.color=always)

ARGS_BASE=(-P pr-checks -DskipTests=true)
# Exclude system-tests by default for lint pass
ARGS_SCOPE=(-pl '!modules/system-tests' -am)

if [[ -n "${THREADS}" ]]; then ARGS_BASE+=(-T "${THREADS}"); fi
if [[ -n "${MODULE}"  ]]; then ARGS_SCOPE=(-pl "${MODULE}" -am); fi
if [[ -n "${EXTRA}"   ]]; then read -r -a EXTRA_ARR <<< "${EXTRA}"; else EXTRA_ARR=(); fi

echo "[lint] Using backend dir: ${BACKEND_DIR}"
java -version || true

if [[ "${MODE}" == "fix" ]]; then
  echo "[lint] Applying auto-fixes (if configured, e.g., Spotless)..."
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS_BASE[@]}" "${ARGS_SCOPE[@]}" "${EXTRA_ARR[@]}" spotless:apply || true )
fi

echo "[lint] Running static checks (pr-checks profile)"
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS_BASE[@]}" "${ARGS_SCOPE[@]}" "${EXTRA_ARR[@]}" clean verify )
