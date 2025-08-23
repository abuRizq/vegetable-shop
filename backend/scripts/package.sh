#!/usr/bin/env bash
# package.sh — Build JARs and container image for Vegetable Shop Backend
#
# Features:
# - Builds with Maven (wrapper if available) using backend/ci/maven-settings.xml by default
# - Supports Spring Boot buildpacks or classic Dockerfile builds
# - Optional push to container registry (GHCR or custom)
# - Optional SBOM generation if scripts/sbom-generate.sh exists
#
# Usage:
#   ./scripts/package.sh [options]
#
# Options:
#   --profiles <csv>     Maven profiles (default: ci)
#   --threads <N|1C>     Maven parallelism
#   --settings <path>    Custom settings.xml (default: backend/ci/maven-settings.xml if present)
#   --module <selector>  App module to package (default: apps/veggieshop-service)
#   --skip-tests         Skip all tests
#   --buildpacks         Use Spring Boot buildpacks (default)
#   --dockerfile         Use Dockerfile build (docker/app/Dockerfile)
#   --image <name>       Image name (default: ghcr.io/${GITHUB_REPOSITORY}/veggieshop-service)
#   --tag <tag>          Image tag (default: git tag or timestamp)
#   --push               Push the image after build
#   --latest             Also push the 'latest' tag (with --push)
#   --sbom               Try to generate SBOM (best effort)
#   --debug              Print executed commands
#   -h, --help           Show this help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

PROFILES="ci"
THREADS=""
SETTINGS=""
MODULE="apps/veggieshop-service"
SKIP_TESTS=0
USE_BUILDPACKS=1
IMAGE="${IMAGE:-}"
TAG="${TAG:-}"
DO_PUSH=0
PUSH_LATEST=0
DO_SBOM=0
DEBUG=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profiles)  PROFILES="$2"; shift 2;;
    --threads)   THREADS="$2"; shift 2;;
    --settings)  SETTINGS="$2"; shift 2;;
    --module)    MODULE="$2"; shift 2;;
    --skip-tests) SKIP_TESTS=1; shift;;
    --buildpacks) USE_BUILDPACKS=1; shift;;
    --dockerfile) USE_BUILDPACKS=0; shift;;
    --image)     IMAGE="$2"; shift 2;;
    --tag)       TAG="$2"; shift 2;;
    --push)      DO_PUSH=1; shift;;
    --latest)    PUSH_LATEST=1; shift;;
    --sbom)      DO_SBOM=1; shift;;
    --debug)     DEBUG=1; shift;;
    -h|--help)   usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

# Defaults for image and tag
if [[ -z "${IMAGE}" ]]; then
  if [[ -n "${GITHUB_REPOSITORY:-}" ]]; then
    IMAGE="ghcr.io/${GITHUB_REPOSITORY}/veggieshop-service"
  else
    IMAGE="veggieshop/veggieshop-service"
  fi
fi

if [[ -z "${TAG}" ]]; then
  if git -C "${BACKEND_DIR}" describe --tags --exact-match >/dev/null 2>&1; then
    TAG="$(git -C "${BACKEND_DIR}" describe --tags --exact-match)"
  else
    TAG="$(date +%Y%m%d%H%M%S)"
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

ARGS_BUILD=(clean package -P "${PROFILES}")
if [[ -n "${THREADS}" ]]; then ARGS_BUILD+=(-T "${THREADS}"); fi
if [[ "${SKIP_TESTS}" -eq 1 ]]; then ARGS_BUILD+=(-DskipTests=true); fi
ARGS_BUILD+=(-pl "${MODULE}" -am)

echo "[package] Building JARs with Maven: ${MVN_BIN[*]} ${ARGS_BUILD[*]}"
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS_BUILD[@]}" )

# Build image
if [[ "${USE_BUILDPACKS}" -eq 1 ]]; then
  echo "[package] Building image via Spring Boot buildpacks -> ${IMAGE}:${TAG}"
  ARGS_IMG=(-pl "${MODULE}" -am -DskipTests=true spring-boot:build-image -Dspring-boot.build-image.imageName="${IMAGE}:${TAG}")
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ARGS_IMG[@]}" )
else
  echo "[package] Building image via Dockerfile -> ${IMAGE}:${TAG}"
  DOCKERFILE="${BACKEND_DIR}/docker/app/Dockerfile"
  CONTEXT_DIR="${BACKEND_DIR}"
  docker build -f "${DOCKERFILE}" -t "${IMAGE}:${TAG}" "${CONTEXT_DIR}"
fi

# SBOM generation (best effort)
if [[ "${DO_SBOM}" -eq 1 ]]; then
  echo "[package] Generating SBOM (best effort)"
  chmod +x "${BACKEND_DIR}/scripts/sbom-generate.sh" 2>/dev/null || true
  "${BACKEND_DIR}/scripts/sbom-generate.sh" || true
fi

# Push image
if [[ "${DO_PUSH}" -eq 1 ]]; then
  echo "[package] Pushing ${IMAGE}:${TAG}"
  if [[ "${IMAGE}" == ghcr.io/* ]]; then
    echo "[package] Logging in to GHCR"
    echo "${GITHUB_TOKEN:-${CR_PAT:-}}" | docker login ghcr.io -u "${GITHUB_ACTOR:-${CR_USERNAME:-'github-actions'}}" --password-stdin
  fi
  docker push "${IMAGE}:${TAG}"
  if [[ "${PUSH_LATEST}" -eq 1 ]]; then
    docker tag "${IMAGE}:${TAG}" "${IMAGE}:latest"
    docker push "${IMAGE}:latest"
  fi
fi

echo "[package] Done. Image: ${IMAGE}:${TAG}"
