#!/usr/bin/env bash
# wait-for-it.sh — Minimal, dependency-friendly TCP wait script
# Usage:
#   wait-for-it.sh host:port [-t timeout] [--strict] [-q] [-- command args...]
#   wait-for-it.sh -h host -p port [-t timeout] [--strict] [-q] [-- command args...]
#
# Notes:
# - Uses `nc` if available; falls back to bash's /dev/tcp when possible.
# - When --strict is set, the script exits non‑zero on timeout and does NOT run the command.
# - Without --strict, timing out will still run the command (if provided).
# - Timeout is in seconds (default: 60).
#
set -Eeuo pipefail

# Globals
HOST=""
PORT=""
TIMEOUT=60
STRICT=0
QUIET=0

usage() {
  cat <<'USAGE'
wait-for-it.sh — wait for a TCP host:port to become available

Usage:
  wait-for-it.sh host:port [-t timeout] [--strict] [-q] [-- command args...]
  wait-for-it.sh -h host -p port [-t timeout] [--strict] [-q] [-- command args...]

Options:
  -h HOST        Hostname or IP
  -p PORT        Port number
  -t SECONDS     Timeout in seconds (default: 60)
  --strict       Exit non-zero on timeout (do not run the command)
  -q, --quiet    Quiet mode (suppress logs)
  --             Everything after -- is executed once the host:port is ready

Examples:
  ./wait-for-it.sh postgres:5432 -t 90 --strict -- echo "DB is up"
  ./wait-for-it.sh -h kafka -p 9092 -- ./scripts/start-app.sh
USAGE
}

log() {
  if [[ "${QUIET}" -eq 0 ]]; then
    printf '[wait-for-it] %s\n' "$*" >&2
  fi
}

has_cmd() { command -v "$1" >/dev/null 2>&1; }

# Parse args
if [[ "${#}" -eq 0 ]]; then
  usage; exit 1
fi

POSITIONAL=()
if [[ "${1:-}" == *:* ]]; then
  # form: host:port as first arg
  IFS=':' read -r HOST PORT <<< "${1}"
  shift
fi

while [[ "${#}" -gt 0 ]]; do
  case "$1" in
    -h) HOST="${2:-}"; shift 2 ;;
    -p) PORT="${2:-}"; shift 2 ;;
    -t) TIMEOUT="${2:-}"; shift 2 ;;
    --strict) STRICT=1; shift ;;
    -q|--quiet) QUIET=1; shift ;;
    --) shift; POSITIONAL+=("$@"); break ;;
    -h|--help) usage; exit 0 ;;
    *) usage; exit 1 ;;
  esac
done

if [[ -z "${HOST}" || -z "${PORT}" ]]; then
  usage; exit 1
fi

deadline=$((SECONDS + TIMEOUT))

log "Waiting for ${HOST}:${PORT} (timeout: ${TIMEOUT}s)..."

check_tcp() {
  if has_cmd nc; then
    nc -z -w 2 "${HOST}" "${PORT}" >/dev/null 2>&1
  else
    # bash TCP check
    (exec 3<>"/dev/tcp/${HOST}/${PORT}") >/dev/null 2>&1 || return 1
    exec 3>&- || true
    return 0
  fi
}

until check_tcp; do
  if (( SECONDS >= deadline )); then
    log "Timeout after ${TIMEOUT}s waiting for ${HOST}:${PORT}"
    if [[ "${STRICT}" -eq 1 ]]; then
      exit 1
    else
      break
    fi
  fi
  sleep 1
done

if [[ "${#POSITIONAL[@]}" -gt 0 ]]; then
  log "Executing command: ${POSITIONAL[*]}"
  exec "${POSITIONAL[@]}"
else
  log "Target is ready."
fi
