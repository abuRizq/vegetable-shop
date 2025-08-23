#!/usr/bin/env bash
# cache-priming.sh — Warm up application caches by hitting common read endpoints
#
# This script sends GET requests against a set of endpoints to prime caches/CDNs.
# Customize endpoints via --endpoints-file or inline defaults below.
#
# Usage:
#   ./scripts/cache-priming.sh [options]
#
# Options:
#   --base-url <url>      Base URL (default: http://localhost:8080)
#   --token <jwt>         Bearer token for Authorization header (optional)
#   --endpoints-file <f>  File with one path per line (comments with '#')
#   --concurrency <n>     Number of parallel requests (default: 4)
#   --timeout <sec>       Curl max-time for each request (default: 10)
#   --retries <n>         Curl retry count (default: 2)
#   --header "K: V"       Extra header (repeatable)
#   --dry-run             Print what would be requested without executing
#   --debug               Print executed commands
#   -h, --help            Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() { sed -n '1,200p' "$0"; }

BASE_URL="http://localhost:8080"
TOKEN=""
ENDPOINTS_FILE=""
CONCURRENCY=4
TIMEOUT=10
RETRIES=2
HEADERS=()
DRY_RUN=0
DEBUG=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base-url) BASE_URL="$2"; shift 2;;
    --token)    TOKEN="$2"; shift 2;;
    --endpoints-file) ENDPOINTS_FILE="$2"; shift 2;;
    --concurrency) CONCURRENCY="$2"; shift 2;;
    --timeout)  TIMEOUT="$2"; shift 2;;
    --retries)  RETRIES="$2"; shift 2;;
    --header)   HEADERS+=(-H "$2"); shift 2;;
    --dry-run)  DRY_RUN=1; shift;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

# Build header list
if [[ -n "${TOKEN}" ]]; then
  HEADERS+=(-H "Authorization: Bearer ${TOKEN}")
fi
HEADERS+=( -H "Accept: application/json" -H "User-Agent: cache-priming/1.0" )

# Default endpoints (safe GETs). Adjust to your real routes.
DEFAULT_ENDPOINTS=(
  "actuator/health"
  "actuator/info"
  "api/catalog/categories"               # example
  "api/catalog/products?size=50&page=0"  # example
  "api/pricing/price-lists"              # example
  "api/inventory/items?size=50&page=0"   # example
  "api/customer/profile"                 # may require auth
  "api/review/recent?size=20"            # example
)

load_endpoints() {
  local arr=()
  if [[ -n "${ENDPOINTS_FILE}" ]]; then
    if [[ ! -f "${ENDPOINTS_FILE}" ]]; then
      echo "[prime] Endpoints file not found: ${ENDPOINTS_FILE}" >&2
      exit 1
    fi
    while IFS= read -r line || [[ -n "$line" ]]; do
      [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
      arr+=("${line}")
    done < "${ENDPOINTS_FILE}"
  else
    arr=("${DEFAULT_ENDPOINTS[@]}")
  fi
  printf "%s\n" "${arr[@]}"
}

do_request() {
  local path="$1"
  local url
  url="${BASE_URL%/}/${path#'/'}"
  if (( DRY_RUN==1 )); then
    echo "[prime][DRY] GET ${url}"
    return 0
  fi
  local out status time_total
  out="$(curl -sS -o /dev/null -w "code=%{http_code} time=%{time_total}\n"         --retry "${RETRIES}" --retry-connrefused --max-time "${TIMEOUT}"         "${HEADERS[@]}" "${url}" || true)"
  status="$(sed -n 's/.*code=\([0-9][0-9][0-9]\).*/\1/p' <<< "${out}")"
  time_total="$(sed -n 's/.*time=\([0-9.]*\).*/\1/p' <<< "${out}")"
  printf "[prime] %-60s -> %3s in %5ss\n" "${path}" "${status:-ERR}" "${time_total:-N/A}"
}

export -f do_request
export BASE_URL RETRIES TIMEOUT DRY_RUN
# For GNU parallel compatibility, stick to xargs
load_endpoints | xargs -I{} -P "${CONCURRENCY}" bash -c 'do_request "$@"' _ {}

echo "[prime] Done."
