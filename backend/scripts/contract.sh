#!/usr/bin/env bash
# contract.sh — Validate & (optionally) generate API/event contracts
#
# Targets the modules/contracts module which contains:
#   - HTTP OpenAPI specs under modules/contracts/http/openapi/*.yaml
#   - Event JSON Schemas under modules/contracts/events/schema/**.json
#   - Optional codegen.sh under modules/contracts/events/ or similar
#
# Usage:
#   ./scripts/contract.sh [validate|generate|all] [options]
#
# Modes:
#   validate (default)  Run Maven validations (e.g., openapi-generator:validate, schema checks) via 'verify'.
#   generate            Run code generation helpers if present (openapi, events).
#   all                 Run validate then generate.
#
# Options:
#   --profiles <csv>     Profiles to use (default: pr-checks)
#   --settings <path>    Custom Maven settings.xml
#   --threads <N|1C>     Maven parallelism
#   --openapi-only       Limit to OpenAPI tasks
#   --events-only        Limit to event schema tasks
#   --debug              Print executed commands
#   -h, --help           Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
CONTRACTS_DIR="${BACKEND_DIR}/modules/contracts"

usage() { sed -n '1,200p' "$0"; }

MODE="validate"
PROFILES="pr-checks"
SETTINGS=""
THREADS=""
OPENAPI_ONLY=0
EVENTS_ONLY=0
DEBUG=0

if [[ "${1:-}" =~ ^(validate|generate|all)$ ]]; then MODE="$1"; shift; fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profiles) PROFILES="$2"; shift 2;;
    --settings) SETTINGS="$2"; shift 2;;
    --threads)  THREADS="$2"; shift 2;;
    --openapi-only) OPENAPI_ONLY=1; shift;;
    --events-only)  EVENTS_ONLY=1; shift;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

if (( OPENAPI_ONLY==1 && EVENTS_ONLY==1 )); then
  echo "Cannot use --openapi-only and --events-only together." >&2
  exit 1
fi

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

run_validate() {
  local args=(verify -P "${PROFILES}" -DskipTests=true -pl modules/contracts -am)
  if [[ -n "${THREADS}" ]]; then args+=(-T "${THREADS}"); fi
  echo "[contract] Validating contracts via Maven: ${MVN_BIN[*]} ${args[*]}"
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${args[@]}" )
}

run_generate_openapi() {
  # If your POM defines openapi-generator goals, they will be executed via 'generate' lifecycle.
  echo "[contract] OpenAPI generation (if configured via plugins)"
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" -pl modules/contracts -am -P "${PROFILES}" -DskipTests=true generate || true )
}

run_generate_events() {
  # If a helper script exists (e.g., modules/contracts/events/codegen.sh), run it.
  local helper="${CONTRACTS_DIR}/events/codegen.sh"
  if [[ -x "${helper}" ]]; then
    echo "[contract] Running event codegen helper: ${helper}"
    ( cd "${BACKEND_DIR}" && "${helper}" )
  else
    echo "[contract] No events codegen helper found at ${helper}; skipping."
  fi
}

case "${MODE}" in
  validate)
    run_validate
    ;;
  generate)
    if [[ "${EVENTS_ONLY}" -eq 1 ]]; then
      run_generate_events
    elif [[ "${OPENAPI_ONLY}" -eq 1 ]]; then
      run_generate_openapi
    else
      run_generate_openapi
      run_generate_events
    fi
    ;;
  all)
    run_validate
    if [[ "${EVENTS_ONLY}" -eq 1 ]]; then
      run_generate_events
    elif [[ "${OPENAPI_ONLY}" -eq 1 ]]; then
      run_generate_openapi
    else
      run_generate_openapi
      run_generate_events
    fi
    ;;
esac
