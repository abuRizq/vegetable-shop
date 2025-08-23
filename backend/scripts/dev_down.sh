#!/usr/bin/env bash
# dev_down.sh — Stop local development infrastructure (Docker Compose)
#
# Usage:
#   ./scripts/dev_down.sh [options] [service ...]
#
# Options:
#   --compose <path>     Compose file path (default: backend/config/local/docker-compose.yml)
#   --.env-file <path>    ..env file path (default: backend/..env if exists)
#   --rm                 Remove stopped containers (docker compose rm -fsv) when services provided
#   --volumes            Remove named volumes (adds -v to 'down')
#   --rmi <local|all>    Remove images (adds --rmi to 'down')
#   --remove-orphans     Remove orphan containers
#   --prune              Run 'docker system prune -f' after down
#   --debug              Print executed commands
#   -h, --help           Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

COMPOSE_FILE="${BACKEND_DIR}/config/local/docker-compose.yml"
ENV_FILE="${BACKEND_DIR}/.env"
DO_RM=0
VOLUMES=0
RMI=""
REMOVE_ORPHANS=0
PRUNE=0
DEBUG=0

SERVICES=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose)  COMPOSE_FILE="$2"; shift 2;;
    --.env-file) ENV_FILE="$2"; shift 2;;
    --rm)       DO_RM=1; shift;;
    --volumes)  VOLUMES=1; shift;;
    --rmi)      RMI="$2"; shift 2;;
    --remove-orphans) REMOVE_ORPHANS=1; shift;;
    --prune)    PRUNE=1; shift;;
    --debug)    DEBUG=1; shift;;
    -h|--help)  usage; exit 0;;
    *) SERVICES+=("$1"); shift;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "[dev_down] Compose file not found: ${COMPOSE_FILE}" >&2
  exit 1
fi

ENV_OPT=()
if [[ -f "${ENV_FILE}" ]]; then
  ENV_OPT=(--.env-file "${ENV_FILE}")
  echo "[dev_down] Using env file: ${ENV_FILE}"
fi

if [[ "${#SERVICES[@]}" -gt 0 ]]; then
  echo "[dev_down] Stopping services: ${SERVICES[*]}"
  docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" stop "${SERVICES[@]}"
  if (( DO_RM==1 )); then
    echo "[dev_down] Removing services: ${SERVICES[*]}"
    docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" rm -fsv "${SERVICES[@]}"
  fi
else
  echo "[dev_down] docker compose down ..."
  DOWN_ARGS=()
  if (( VOLUMES==1 )); then DOWN_ARGS+=(-v); fi
  if [[ -n "${RMI}" ]]; then DOWN_ARGS+=(--rmi "${RMI}"); fi
  if (( REMOVE_ORPHANS==1 )); then DOWN_ARGS+=(--remove-orphans); fi
  docker compose -f "${COMPOSE_FILE}" "${ENV_OPT[@]}" down "${DOWN_ARGS[@]}"
fi

if (( PRUNE==1 )); then
  echo "[dev_down] Pruning dangling resources ..."
  docker system prune -f || true
fi

echo "[dev_down] Done."
