
# ============================================================================
# VeggieShop Backend - Makefile
# Production-grade helpers for build, test, run, Docker, Helm, and SOPS.
# Usage: make <target> [PROFILE=local] [IMAGE_TAG=local] [REGISTRY=docker.io/you]
# ============================================================================

SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DEFAULT_GOAL := help

# ------------------------
# Project variables
# ------------------------
APP_NAME      ?= veggieshop-backend
APP_MODULE    ?= apps/veggieshop-service
MVNW          ?= ./mvnw
MVN_SETTINGS  ?= ci/maven-settings.xml
PROFILE       ?= local
JAVA_VERSION  ?= 21

# Maven common flags
MVN_COMMON    ?= -s $(MVN_SETTINGS) -T 1C

# Docker/Compose
COMPOSE_FILE  ?= config/local/docker-compose.yml
IMAGE_NAME    ?= veggieshop
IMAGE_TAG     ?= local
REGISTRY      ?=
IMAGE         := $(if $(strip $(REGISTRY)),$(REGISTRY)/,)$(IMAGE_NAME):$(IMAGE_TAG)

# Helm/K8s
HELM_CHART    ?= config/helm/veggie-shop
RELEASE       ?= veggie-shop
NAMESPACE     ?= default
VALUES        ?= $(HELM_CHART)/values-local.yaml

# SOPS
SOPS_CONFIG   ?= .sops.yaml

# Load .env if present (export all)
ifneq (,$(wildcard .env))
include .env
export
endif

# Resolve project version (best-effort; requires mvnw)
VERSION := $(shell $(MVNW) -q help:evaluate -Dexpression=project.version -DforceStdout || echo "0.0.0-SNAPSHOT")

# Colors
Y := \033[33m
G := \033[32m
B := \033[34m
R := \033[31m
X := \033[0m

# --------------------------------------------
# Help
# --------------------------------------------
help: ## Show this help
	@echo -e "$(B)$(APP_NAME) — Make targets$(X)"
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<TARGET>\033[0m [PROFILE=local] [IMAGE_TAG=local] [REGISTRY=]\n\nTargets:\n"} \
	/^[a-zA-Z0-9_\-]+:.*?##/ { printf "  \033[36m%-24s\033[0m %s\n", $$1, $$2 } \
	/^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0,5) } ' $(MAKEFILE_LIST)

print-vars: ## Print key variables
	@echo -e "$(Y)Version$(X): $(VERSION)"
	@echo -e "$(Y)Profile$(X): $(PROFILE)"
	@echo -e "$(Y)Image$(X):   $(IMAGE)"
	@echo -e "$(Y)Namespace$(X): $(NAMESPACE)"
	@echo -e "$(Y)Helm Values$(X): $(VALUES)"

# --------------------------------------------
# Environment & Bootstrap
# --------------------------------------------
env: ## Create .env from example if missing
	@if [ ! -f .env ]; then cp .env.example .env && echo "Created .env"; else echo ".env already exists"; fi

bootstrap: env up build ## Bootstrap local environment (env + infra + build)

# --------------------------------------------
# Infra (Docker Compose)
# --------------------------------------------
up: ## Start local infra (Postgres, Kafka, Nginx, etc.)
	docker compose -f $(COMPOSE_FILE) up -d

down: ## Stop local infra
	docker compose -f $(COMPOSE_FILE) down

logs: ## Tail compose logs
	docker compose -f $(COMPOSE_FILE) logs -f --tail=200

# --------------------------------------------
# Build & Test (Maven)
# --------------------------------------------
build: ## Build all modules (no tests)
	$(MVNW) $(MVN_COMMON) -DskipTests=true clean package

verify: ## Build + run all tests (unit+integration) and checks
	$(MVNW) $(MVN_COMMON) clean verify

test: ## Run unit tests
	$(MVNW) $(MVN_COMMON) -Dtest='*Test' -DfailIfNoTests=false test

it: ## Run integration tests (if separated)
	$(MVNW) $(MVN_COMMON) -DskipUnitTests=true -DfailIfNoTests=false verify

system: ## Run system tests module (Testcontainers)
	$(MVNW) $(MVN_COMMON) -pl modules/system-tests -am test

lint: ## Lint/format check (if configured: Spotless/Checkstyle)
	-$(MVNW) $(MVN_COMMON) spotless:check || true
	-$(MVNW) $(MVN_COMMON) checkstyle:check || true
	@echo -e "$(G)Lint checks completed (best-effort).$(X)"

format: ## Auto-format source (if Spotless configured)
	-$(MVNW) $(MVN_COMMON) spotless:apply || true

dep-tree: ## Print mvn dependency tree (root)
	$(MVNW) $(MVN_COMMON) -Dincludes= -Dverbose=true dependency:tree

# --------------------------------------------
# Packaging & Run
# --------------------------------------------
package: ## Package application (jar)
	$(MVNW) $(MVN_COMMON) -pl $(APP_MODULE) -am -DskipTests clean package

run: ## Run Spring Boot locally with given PROFILE
	$(MVNW) $(MVN_COMMON) -pl $(APP_MODULE) -am spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

stop: ## Stop Spring Boot app (SIGINT)
	@pkill -f "org.springframework.boot.loader.launch.JarLauncher" || true
	@pkill -f "org.springframework.boot.devtools.RemoteSpringApplication" || true
	@echo -e "$(G)Attempted to stop local Spring Boot process.$(X)"

# --------------------------------------------
# Contracts & Codegen
# --------------------------------------------
contracts: ## Generate code from event schemas / OpenAPI (if script exists)
	@if [ -f modules/contracts/events/codegen.sh ]; then \
	  bash modules/contracts/events/codegen.sh; \
	else \
	  echo "No codegen script found at modules/contracts/events/codegen.sh"; \
	fi

# --------------------------------------------
# Database & Migrations
# --------------------------------------------
migrate: ## Run Flyway migrations via app (profile=$(PROFILE)) or migrator image if present
	@echo -e "$(Y)Running migrations (via app startup)...$(X)"
	$(MVNW) $(MVN_COMMON) -pl $(APP_MODULE) -am -Dflyway.migrateAtStart=true spring-boot:run -Dspring-boot.run.profiles=$(PROFILE) &
	APP_PID=$$!; sleep 10; kill $$APP_PID || true

seed: ## Apply SQL seeds in tools/db/seeds/* into local Postgres via docker compose service 'db'
	@echo -e "$(Y)Applying seeds to DB: $(DB_NAME) (service: db)$(X)"
	@for f in tools/db/seeds/*.sql; do \
	  echo "Applying $$f"; \
	  docker compose -f $(COMPOSE_FILE) exec -T db psql -U $(DB_USERNAME) -d $(DB_NAME) -f "/workspace/$$f" 2>/dev/null || true; \
	done

# --------------------------------------------
# Docker Images
# --------------------------------------------
docker-build: package ## Build Docker image for app
	docker build -f docker/app/Dockerfile -t $(IMAGE) .

docker-push: ## Push Docker image (set REGISTRY)
	@if [ -z "$(REGISTRY)" ]; then echo "$(R)REGISTRY is empty. Set REGISTRY to push (e.g., ghcr.io/you).$(X)"; exit 1; fi
	docker push $(IMAGE)

docker-scan: ## Security scan image with Trivy (if available)
	-which trivy >/dev/null 2>&1 && trivy image --exit-code 0 --severity HIGH,CRITICAL $(IMAGE) || echo "Trivy not installed, skipping."

# --------------------------------------------
# Helm (Kubernetes)
# --------------------------------------------
helm-lint: ## Lint Helm chart
	helm lint $(HELM_CHART)

helm-template: ## Render Helm templates with values
	helm template $(RELEASE) $(HELM_CHART) -n $(NAMESPACE) -f $(VALUES)

helm-install: ## Install/upgrade release
	helm upgrade -i $(RELEASE) $(HELM_CHART) -n $(NAMESPACE) -f $(VALUES)

helm-uninstall: ## Uninstall release
	helm uninstall $(RELEASE) -n $(NAMESPACE) || true

# --------------------------------------------
# SOPS helpers
# --------------------------------------------
sops-edit-%: ## Edit a secrets file with sops (e.g., make sops-edit-secrets/dev-app.yaml)
	sops --config $(SOPS_CONFIG) $(subst sops-edit-,,$@)

sops-encrypt-%: ## Encrypt a file in-place with sops (e.g., make sops-encrypt-secrets/dev-app.yaml)
	sops --config $(SOPS_CONFIG) --encrypt --in-place $(subst sops-encrypt-,,$@)

sops-decrypt-%: ## Decrypt a file in-place with sops (e.g., make sops-decrypt-secrets/dev-app.yaml)
	sops --config $(SOPS_CONFIG) --decrypt --in-place $(subst sops-decrypt-,,$@)

# --------------------------------------------
# Documentation & Diagrams
# --------------------------------------------
uml: ## Generate PlantUML PNGs from architecture/*.puml (requires plantuml or docker)
	@if which plantuml >/dev/null 2>&1; then \
	  plantuml -tpng architecture/*.puml -o out; \
	else \
	  docker run --rm -v "$$(pwd)":/workspace ghcr.io/plantuml/plantuml:latest \
	    -tpng /workspace/architecture/*.puml -o /workspace/architecture/out; \
	fi
	@echo -e "$(G)UML diagrams generated under architecture/out$(X)"

docs: ## Open README and architecture tree (print file paths)
	@echo "README: ./README.md"
	@echo "ARCHITECTURE TREE: ./ARCHITECTURE-TREE.md"
	@echo "Guides: ./guides"

# --------------------------------------------
# Cleaning
# --------------------------------------------
clean: ## Clean Maven artifacts
	$(MVNW) $(MVN_COMMON) clean

distclean: clean ## Full cleanup (target dirs + Docker images of this repo tag)
	@find . -name target -type d -prune -exec rm -rf {} +
	@echo -e "$(Y)Removing image $(IMAGE) (if exists)...$(X)"
	-@docker rmi $(IMAGE) 2>/dev/null || true

# --------------------------------------------
# Meta
# --------------------------------------------
.PHONY: help print-vars env bootstrap up down logs \
        build verify test it system lint format dep-tree \
        package run stop contracts migrate seed \
        docker-build docker-push docker-scan \
        helm-lint helm-template helm-install helm-uninstall \
        sops-edit-% sops-encrypt-% sops-decrypt-% \
        uml docs clean distclean
