#!/usr/bin/env bash
# sbom-generate.sh — Generate CycloneDX SBOMs for source (Maven) and optionally container image
#
# Requires: Maven (or ./mvnw). Optionally uses 'syft' or 'trivy' to generate image SBOM.
#
# Usage:
#   ./scripts/sbom-generate.sh [options]
#
# Options:
#   --format <json|xml>   SBOM format for Maven CycloneDX (default: json)
#   --output-dir <path>   Directory to place SBOM files (default: backend/sbom)
#   --module <selector>   Limit to module (e.g., apps/veggieshop-service) for module BOM;
#                         aggregate BOM is always produced at the repo root
#   --image <name:tag>    Additionally produce an image SBOM using 'syft' (preferred) or 'trivy'
#   --profiles <csv>      Maven profiles (default: ci)
#   --settings <path>     Custom Maven settings.xml (default: backend/ci/maven-settings.xml if present)
#   --threads <N|1C>      Maven parallelism
#   --debug               Print executed commands
#   -h, --help            Show help
#
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

usage() { sed -n '1,200p' "$0"; }

FORMAT="json"
OUT_DIR="${BACKEND_DIR}/sbom"
MODULE_SELECTOR=""
IMAGE_NAME=""
PROFILES="ci"
SETTINGS=""
THREADS=""
DEBUG=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --format) FORMAT="$2"; shift 2;;
    --output-dir) OUT_DIR="$2"; shift 2;;
    --module) MODULE_SELECTOR="$2"; shift 2;;
    --image) IMAGE_NAME="$2"; shift 2;;
    --profiles) PROFILES="$2"; shift 2;;
    --settings) SETTINGS="$2"; shift 2;;
    --threads) THREADS="$2"; shift 2;;
    --debug) DEBUG=1; shift;;
    -h|--help) usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

[[ "${DEBUG}" -eq 1 ]] && set -x

mkdir -p "${OUT_DIR}"

MVNW="${BACKEND_DIR}/mvnw"
MVN_BIN=()
if [[ -x "${MVNW}" ]]; then MVN_BIN+=("${MVNW}"); else MVN_BIN+=("mvn"); fi

if [[ -n "${SETTINGS}" ]]; then
  MVN_BIN+=(-s "${SETTINGS}")
elif [[ -f "${BACKEND_DIR}/ci/maven-settings.xml" ]]; then
  MVN_BIN+=(-s "${BACKEND_DIR}/ci/maven-settings.xml")
fi

MVN_BIN+=(-B -V -U -Dstyle.color=always)

# 1) Aggregate SBOM for the entire multi-module build
echo "[sbom] Generating aggregate CycloneDX SBOM (${FORMAT}) at root"
ROOT_ARGS=(-P "${PROFILES}" -DskipTests=true -Dcyclonedx.skipAttach=false -Dcyclonedx.outputFormat="${FORMAT}" -Dcyclonedx.includeBomSerialNumber=true)
if [[ -n "${THREADS}" ]]; then ROOT_ARGS+=(-T "${THREADS}"); fi
( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${ROOT_ARGS[@]}" org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom )

# Move aggregate output(s)
shopt -s nullglob
for f in "${BACKEND_DIR}"/target/*cyclonedx*."${FORMAT}"; do
  cp -f "$f" "${OUT_DIR}/$(basename "$f")"
done

# 2) Optional module-specific SBOM
if [[ -n "${MODULE_SELECTOR}" ]]; then
  echo "[sbom] Generating module SBOM for '${MODULE_SELECTOR}'"
  MOD_ARGS=(-P "${PROFILES}" -DskipTests=true -Dcyclonedx.skipAttach=false -Dcyclonedx.outputFormat="${FORMAT}" -pl "${MODULE_SELECTOR}" -am)
  if [[ -n "${THREADS}" ]]; then MOD_ARGS+=(-T "${THREADS}"); fi
  ( cd "${BACKEND_DIR}" && "${MVN_BIN[@]}" "${MOD_ARGS[@]}" org.cyclonedx:cyclonedx-maven-plugin:makeBom )
  for f in "${BACKEND_DIR}"/**/target/*cyclonedx*."${FORMAT}"; do
    # add module name prefix
    base="$(basename "$f")"
    modprefix="$(basename "$(dirname "$(dirname "$f")")")"
    cp -f "$f" "${OUT_DIR}/${modprefix}-${base}"
  done
fi

# 3) Optional container image SBOM
if [[ -n "${IMAGE_NAME}" ]]; then
  echo "[sbom] Generating image SBOM for ${IMAGE_NAME}"
  if command -v syft >/dev/null 2>&1; then
    syft "${IMAGE_NAME}" -o "cyclonedx-${FORMAT}" > "${OUT_DIR}/image-${IMAGE_NAME//[:\/]/_}-cyclonedx.${FORMAT}" || true
  elif command -v trivy >/dev/null 2>&1; then
    if [[ "${FORMAT}" == "json" ]]; then
      trivy image --format cyclonedx --output "${OUT_DIR}/image-${IMAGE_NAME//[:\/]/_}-cyclonedx.json" "${IMAGE_NAME}" || true
    else
      echo "[sbom] trivy supports CycloneDX JSON only; falling back to JSON."
      trivy image --format cyclonedx --output "${OUT_DIR}/image-${IMAGE_NAME//[:\/]/_}-cyclonedx.json" "${IMAGE_NAME}" || true
    fi
  else
    echo "[sbom] Neither 'syft' nor 'trivy' found; skipping image SBOM."
  fi
fi

echo "[sbom] SBOMs written to: ${OUT_DIR}"
