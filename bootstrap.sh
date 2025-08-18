#!/usr/bin/env bash
set -Eeuo pipefail

BASE_DIR="backend"

log() { printf -- "• %s\n" "$*"; }

mkd() { mkdir -p "$1"; }
mkf() { : > "$1"; }  # create/truncate (empty) file

# --- Root ---------------------------------------------------------------------
log "Creating root tree under $BASE_DIR"
mkd "$BASE_DIR"

# Top-level files
for f in \
  pom.xml .editorconfig .gitattributes .gitignore .dockerignore .java-version \
  .env.example .sops.yaml LICENSE CODE_OF_CONDUCT.md CONTRIBUTING.md README.md \
  ARCHITECTURE-TREE.md Makefile mvnw mvnw.cmd
do mkf "$BASE_DIR/$f"; done

# .mvn/wrapper
mkd "$BASE_DIR/.mvn/wrapper"
for f in maven-wrapper.properties maven-wrapper.jar MavenWrapperDownloader.java
do mkf "$BASE_DIR/.mvn/wrapper/$f"; done

# --- Docs / ADR / Architecture / Guides --------------------------------------
log "Creating docs/adr/architecture/guides"
mkd "$BASE_DIR/docs/decision-logs"
mkf "$BASE_DIR/docs/decision-logs/README.md"

mkd "$BASE_DIR/adr"
for f in \
  ADR-0001-shared-kernel.md ADR-0002-flyway-single-module.md \
  ADR-0003-problem-details-only.md ADR-0004-outbox-pattern.md \
  ADR-0005-archunit-boundaries.md ADR-0006-ports-and-adapters.md
do mkf "$BASE_DIR/adr/$f"; done

mkd "$BASE_DIR/architecture"
for f in \
  C1-system-context.puml C2-container.puml C3-component.puml \
  catalog-sequence-checkout.puml database-erd.puml
do mkf "$BASE_DIR/architecture/$f"; done

mkd "$BASE_DIR/guides"
for f in conventions.md local-dev.md troubleshooting.md release-process.md
do mkf "$BASE_DIR/guides/$f"; done

# --- CI / GitHub --------------------------------------------------------------
log "Creating CI/GitHub"
mkd "$BASE_DIR/ci"
mkf "$BASE_DIR/ci/maven-settings.xml"

mkd "$BASE_DIR/.github/workflows"
mkf "$BASE_DIR/.github/CODEOWNERS"
mkf "$BASE_DIR/.github/dependabot.yml"
for f in build.yml pr-checks.yml release.yml security.yml
do mkf "$BASE_DIR/.github/workflows/$f"; done

# --- Scripts ------------------------------------------------------------------
log "Creating scripts"
mkd "$BASE_DIR/scripts"
for f in \
  wait-for-it.sh build.sh test.sh lint.sh it.sh contract.sh package.sh \
  migrate.sh dev_up.sh dev_down.sh cache-priming.sh sbom-generate.sh
do mkf "$BASE_DIR/scripts/$f"; done

# --- Docker -------------------------------------------------------------------
log "Creating docker"
mkd "$BASE_DIR/docker/app"
mkf "$BASE_DIR/docker/app/Dockerfile"

mkd "$BASE_DIR/docker/infra/nginx"
mkf "$BASE_DIR/docker/infra/nginx/Dockerfile"

mkd "$BASE_DIR/docker/infra/migrator"
mkf "$BASE_DIR/docker/infra/migrator/Dockerfile"

# --- Config (local + Helm) ----------------------------------------------------
log "Creating config (local + helm)"
mkd "$BASE_DIR/config/local/nginx"
mkf "$BASE_DIR/config/local/nginx/default.conf"
mkf "$BASE_DIR/config/local/docker-compose.yml"

mkd "$BASE_DIR/config/helm/veggie-shop/templates"
for f in Chart.yaml values.yaml values-local.yaml values-prod.yaml
do mkf "$BASE_DIR/config/helm/veggie-shop/$f"; done

for f in deployment.yaml service.yaml ingress.yaml external-secret.yaml hpa.yaml pdb.yaml secrets.yaml.gone.README
do mkf "$BASE_DIR/config/helm/veggie-shop/templates/$f"; done

# --- Secrets ------------------------------------------------------------------
log "Creating secrets"
mkd "$BASE_DIR/secrets"
for f in README.md dev-app.yaml staging-app.yaml
do mkf "$BASE_DIR/secrets/$f"; done

# --- Tools --------------------------------------------------------------------
log "Creating tools (codegen + db seeds)"
mkd "$BASE_DIR/tools/codegen/dto-templates"
for f in dto.mustache request.mustache
do mkf "$BASE_DIR/tools/codegen/dto-templates/$f"; done

mkd "$BASE_DIR/tools/codegen/mapper-templates"
for f in mapper.mustache page-mapper.mustache
do mkf "$BASE_DIR/tools/codegen/mapper-templates/$f"; done

mkd "$BASE_DIR/tools/db/seeds"
for f in 001_categories.sql 002_products.sql 003_users.sql
do mkf "$BASE_DIR/tools/db/seeds/$f"; done

# --- Modules: domain-kernel ---------------------------------------------------
log "Creating modules/domain-kernel"
mkd "$BASE_DIR/modules/domain-kernel"
mkf "$BASE_DIR/modules/domain-kernel/pom.xml"
for path in \
  src/main/java/com/veggieshop/shared/validation \
  src/main/java/com/veggieshop/shared/domain/model \
  src/main/java/com/veggieshop/shared/domain/enums
do mkd "$BASE_DIR/modules/domain-kernel/$path"; done

for f in Preconditions.java Validators.java
do mkf "$BASE_DIR/modules/domain-kernel/src/main/java/com/veggieshop/shared/validation/$f"; done

for f in Money.java Weight.java Dimensions.java Address.java ContactInfo.java GeoLocation.java
do mkf "$BASE_DIR/modules/domain-kernel/src/main/java/com/veggieshop/shared/domain/model/$f"; done

for f in Currency.java UnitOfMeasure.java TemperatureZone.java
do mkf "$BASE_DIR/modules/domain-kernel/src/main/java/com/veggieshop/shared/domain/enums/$f"; done

# --- Modules: platform-autoconfigure + starter + platform ---------------------
log "Creating modules/platform-*"
mkd "$BASE_DIR/modules/platform-autoconfigure/src/main/java/com/veggieshop/platform/autoconfigure"
mkd "$BASE_DIR/modules/platform-autoconfigure/src/main/resources/META-INF/spring"
mkf "$BASE_DIR/modules/platform-autoconfigure/pom.xml"
for f in WebErrorAutoConfiguration.java SecurityAutoConfiguration.java ObservabilityAutoConfiguration.java PersistenceAutoConfiguration.java
do mkf "$BASE_DIR/modules/platform-autoconfigure/src/main/java/com/veggieshop/platform/autoconfigure/$f"; done
mkf "$BASE_DIR/modules/platform-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"

mkd "$BASE_DIR/modules/platform-starter"
mkf "$BASE_DIR/modules/platform-starter/pom.xml"
mkf "$BASE_DIR/modules/platform-starter/README.md"

mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/web/error"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/web/filters"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/observability"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/security"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/persistence/repository"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/persistence/config"
mkd "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/config"
mkf "$BASE_DIR/modules/platform/pom.xml"

mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/web/GlobalResponseAdvice.java"
for f in ProblemDetails.java ProblemDetails422.java ApiResponses422.java ErrorEnvelope.java ErrorShape.java ErrorResponseFactory.java ExceptionMappingService.java DefaultExceptionMappingService.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/web/error/$f"; done
mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/web/filters/TraceIdFilter.java"

for f in TraceIdGenerator.java MdcThreadPoolTaskExecutor.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/observability/$f"; done

for f in SecurityConfig.java RestAuthenticationEntryPoint.java RestAccessDeniedHandler.java PasswordPolicy.java RequestRateLimiter.java SecurityConstants.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/security/$f"; done

for f in BaseEntity.java SoftDeletable.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/persistence/$f"; done

for f in BaseRepository.java CustomJpaRepositoryImpl.java Specifications.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/persistence/repository/$f"; done

for f in JpaAuditingConfig.java CacheConfig.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/persistence/config/$f"; done

for f in OpenApiConfig.java CORSConfig.java ErrorProps.java AsyncConfig.java
do mkf "$BASE_DIR/modules/platform/src/main/java/com/veggieshop/platform/config/$f"; done

# --- Modules: messaging -------------------------------------------------------
log "Creating modules/messaging"
mkd "$BASE_DIR/modules/messaging/src/main/java/com/veggieshop/messaging/outbox"
mkd "$BASE_DIR/modules/messaging/src/main/java/com/veggieshop/messaging/kafka"
mkf "$BASE_DIR/modules/messaging/pom.xml"
for f in OutboxRecord.java OutboxRepository.java OutboxService.java OutboxPublisher.java OutboxRelay.java TransactionalOutboxAspect.java
do mkf "$BASE_DIR/modules/messaging/src/main/java/com/veggieshop/messaging/outbox/$f"; done
for f in KafkaConfig.java KafkaOutboxPublisher.java
do mkf "$BASE_DIR/modules/messaging/src/main/java/com/veggieshop/messaging/kafka/$f"; done

# --- Modules: migrations (Flyway) ---------------------------------------------
log "Creating modules/migrations"
mkd "$BASE_DIR/modules/migrations/src/main/resources/db/migration"
mkf "$BASE_DIR/modules/migrations/pom.xml"
for f in \
  V1__baseline_auth_core.sql V2__catalog_inventory.sql V3__pricing_order.sql \
  V4__customer_media_review.sql V5__notification_audit.sql \
  V6__indexes_constraints.sql V7__outbox_inbox_jobs.sql
do mkf "$BASE_DIR/modules/migrations/src/main/resources/db/migration/$f"; done

# --- Modules: contracts (OpenAPI + Events) ------------------------------------
log "Creating modules/contracts"
mkd "$BASE_DIR/modules/contracts/events/schema/order"
mkd "$BASE_DIR/modules/contracts/events/schema/inventory"
mkd "$BASE_DIR/modules/contracts/events/schema/notification"
mkd "$BASE_DIR/modules/contracts/http/openapi"
mkf "$BASE_DIR/modules/contracts/pom.xml"
for f in OrderCreated.json OrderPaid.json
do mkf "$BASE_DIR/modules/contracts/events/schema/order/$f"; done
mkf "$BASE_DIR/modules/contracts/events/schema/inventory/StockReserved.json"
mkf "$BASE_DIR/modules/contracts/events/schema/notification/NotificationRequested.json"
mkf "$BASE_DIR/modules/contracts/events/codegen.sh"
for f in \
  auth.yaml catalog.yaml inventory.yaml order.yaml pricing.yaml customer.yaml \
  checkout.yaml vendor.yaml media.yaml review.yaml notification.yaml audit.yaml
do mkf "$BASE_DIR/modules/contracts/http/openapi/$f"; done

# --- Modules: testing ---------------------------------------------------------
log "Creating modules/testing"
mkd "$BASE_DIR/modules/testing/src/test/java/com/veggieshop/testing"
mkf "$BASE_DIR/modules/testing/pom.xml"
for f in ArchRules.java RestAssuredBase.java ProblemDetailsContract.java WireMockSupport.java
do mkf "$BASE_DIR/modules/testing/src/test/java/com/veggieshop/testing/$f"; done

# --- Modules: system-tests ----------------------------------------------------
log "Creating modules/system-tests"
mkd "$BASE_DIR/modules/system-tests/src/test/java/com/veggieshop/system"
mkf "$BASE_DIR/modules/system-tests/pom.xml"
for f in TestcontainersConfig.java FlywayIT.java RepositoryIT.java ApiFlowIT.java OpenApiContractTest.java
do mkf "$BASE_DIR/modules/system-tests/src/test/java/com/veggieshop/system/$f"; done

# --- Helper to generate a context module --------------------------------------
make_context () {
  local name="$1"; shift
  log "Creating context: $name"
  local base="$BASE_DIR/modules/contexts/$name"
  mkd "$base"
  mkf "$base/pom.xml"

  # conventional tree
  local dom="$base/src/main/java/com/veggieshop/${name#context-}"
  # path segments vary; we will place per-name specializations below
}

# Because each context has its own structure and many files, define per-context:
context_auth () {
  local base="$BASE_DIR/modules/contexts/context-auth"
  mkd "$base/src/main/java/com/veggieshop/auth/domain/model"
  mkd "$base/src/main/java/com/veggieshop/auth/domain/enums"
  mkd "$base/src/main/java/com/veggieshop/auth/application/port"
  mkd "$base/src/main/java/com/veggieshop/auth/application/service"
  mkd "$base/src/main/java/com/veggieshop/auth/application/impl"
  mkd "$base/src/main/java/com/veggieshop/auth/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/auth/infrastructure/persistence/adapter"
  mkd "$base/src/main/java/com/veggieshop/auth/infrastructure/security/adapters"
  mkd "$base/src/main/java/com/veggieshop/auth/infrastructure/security"
  mkd "$base/src/main/java/com/veggieshop/auth/api/http/controllers"
  mkd "$base/src/main/java/com/veggieshop/auth/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/auth/api/http/mapper"
  mkf "$base/pom.xml"

  for f in User.java Role.java Permission.java OAuthAccount.java RefreshToken.java VerificationToken.java PasswordResetToken.java LoginSession.java TrustedDevice.java
  do mkf "$base/src/main/java/com/veggieshop/auth/domain/model/$f"; done

  for f in AuthProvider.java MfaType.java SessionStatus.java
  do mkf "$base/src/main/java/com/veggieshop/auth/domain/enums/$f"; done

  for f in UserRepositoryPort.java TokenRepositoryPort.java OAuthClientPort.java MfaProviderPort.java
  do mkf "$base/src/main/java/com/veggieshop/auth/application/port/$f"; done

  for f in AuthService.java UserService.java TokenService.java PasswordService.java MfaService.java OAuthService.java DeviceService.java SessionService.java
  do mkf "$base/src/main/java/com/veggieshop/auth/application/service/$f"; done

  for f in AuthServiceImpl.java UserServiceImpl.java TokenServiceImpl.java PasswordServiceImpl.java MfaServiceImpl.java OAuthServiceImpl.java DeviceServiceImpl.java SessionServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/auth/application/impl/$f"; done

  for f in UserJpaRepository.java TokenJpaRepository.java LoginSessionJpaRepository.java
  do mkf "$base/src/main/java/com/veggieshop/auth/infrastructure/persistence/jpa/$f"; done

  for f in UserRepositoryAdapter.java TokenRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/auth/infrastructure/persistence/adapter/$f"; done

  for f in JwtTokenProvider.java JwtAuthenticationFilter.java
  do mkf "$base/src/main/java/com/veggieshop/auth/infrastructure/security/$f"; done

  for f in OAuthGoogleClient.java MfaTotpProvider.java
  do mkf "$base/src/main/java/com/veggieshop/auth/infrastructure/security/adapters/$f"; done

  for f in AuthController.java PasswordController.java VerificationController.java MfaController.java OAuthController.java SessionController.java
  do mkf "$base/src/main/java/com/veggieshop/auth/api/http/controllers/$f"; done

  for f in LoginRequest.java RegisterRequest.java TokenResponse.java RefreshTokenRequest.java MeDTO.java ChangePasswordRequest.java VerifyEmailRequest.java InitiatePasswordResetRequest.java CompletePasswordResetRequest.java MfaSetupDTO.java MfaVerifyRequest.java DeviceRegistrationRequest.java OAuthLinkRequest.java RevokeSessionRequest.java
  do mkf "$base/src/main/java/com/veggieshop/auth/api/http/dto/$f"; done

  for f in UserMapper.java DeviceMapper.java
  do mkf "$base/src/main/java/com/veggieshop/auth/api/http/mapper/$f"; done
}

context_catalog () {
  local base="$BASE_DIR/modules/contexts/context-catalog"
  mkd "$base/src/main/java/com/veggieshop/catalog/domain"
  mkd "$base/src/main/java/com/veggieshop/catalog/application/port"
  mkd "$base/src/main/java/com/veggieshop/catalog/application/service"
  mkd "$base/src/main/java/com/veggieshop/catalog/application/impl"
  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/jpa/projections"
  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/specs"
  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/catalog/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/catalog/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/catalog/api/http/mapper"
  mkf "$base/pom.xml"

  for f in Category.java Product.java ProductVariety.java ProductAttribute.java NutritionProfile.java Allergen.java Tag.java ProductImage.java OrganicCertification.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/domain/$f"; done

  for f in CategoryRepositoryPort.java ProductRepositoryPort.java TagRepositoryPort.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/application/port/$f"; done

  for f in CategoryService.java ProductService.java TagService.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/application/service/$f"; done

  for f in CategoryServiceImpl.java ProductServiceImpl.java TagServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/application/impl/$f"; done

  for f in CategoryRepository.java ProductRepository.java TagRepository.java ProductImageRepository.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/jpa/$f"; done

  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/jpa/projections"
  mkf "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/jpa/projections/ProductSummaryProjection.java"
  mkd "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/specs"
  mkf "$base/src/main/java/com/veggieshop/catalog/infrastructure/persistence/specs/ProductSpecifications.java"

  for f in CategoryRepositoryAdapter.java ProductRepositoryAdapter.java TagRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/infrastructure/adapter/$f"; done

  for f in ProductController.java CategoryController.java TagController.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/api/http/controller/$f"; done

  for f in CategoryDTO.java CategoryTreeDTO.java ProductDTO.java ProductSummaryDTO.java ProductImageDTO.java TagDTO.java NutritionProfileDTO.java OrganicCertificationDTO.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/api/http/dto/$f"; done

  for f in CategoryMapper.java ProductMapper.java ProductImageMapper.java TagMapper.java NutritionProfileMapper.java OrganicCertificationMapper.java
  do mkf "$base/src/main/java/com/veggieshop/catalog/api/http/mapper/$f"; done
}

context_inventory () {
  local base="$BASE_DIR/modules/contexts/context-inventory"
  mkd "$base/src/main/java/com/veggieshop/inventory/domain"
  mkd "$base/src/main/java/com/veggieshop/inventory/application/port"
  mkd "$base/src/main/java/com/veggieshop/inventory/application/service"
  mkd "$base/src/main/java/com/veggieshop/inventory/application/impl"
  mkd "$base/src/main/java/com/veggieshop/inventory/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/inventory/infrastructure/persistence/adapter"
  mkd "$base/src/main/java/com/veggieshop/inventory/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/inventory/api/http/dto/request"
  mkd "$base/src/main/java/com/veggieshop/inventory/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/inventory/api/http/mapper"
  mkf "$base/pom.xml"

  for f in Warehouse.java StockBatch.java InventoryItem.java StockMovement.java ReorderRule.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/domain/$f"; done

  for f in WarehouseRepositoryPort.java InventoryItemRepositoryPort.java StockBatchRepositoryPort.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/application/port/$f"; done

  for f in InventoryService.java StockReservationService.java WarehouseService.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/application/service/$f"; done

  for f in InventoryServiceImpl.java StockReservationServiceImpl.java WarehouseServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/application/impl/$f"; done

  for f in WarehouseRepository.java InventoryItemRepository.java StockBatchRepository.java StockMovementRepository.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/infrastructure/persistence/jpa/$f"; done

  for f in WarehouseRepositoryAdapter.java InventoryItemRepositoryAdapter.java StockBatchRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/infrastructure/persistence/adapter/$f"; done

  for f in InventoryController.java StockReservationController.java WarehouseController.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/api/http/controller/$f"; done

  for f in WarehouseDTO.java InventoryItemDTO.java StockBatchDTO.java StockMovementDTO.java ReorderRuleDTO.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/api/http/dto/$f"; done

  for f in ReserveStockRequest.java AdjustStockRequest.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/api/http/dto/request/$f"; done

  for f in WarehouseMapper.java InventoryItemMapper.java StockBatchMapper.java
  do mkf "$base/src/main/java/com/veggieshop/inventory/api/http/mapper/$f"; done
}

context_pricing () {
  local base="$BASE_DIR/modules/contexts/context-pricing"
  mkd "$base/src/main/java/com/veggieshop/pricing/domain"
  mkd "$base/src/main/java/com/veggieshop/pricing/application/port"
  mkd "$base/src/main/java/com/veggieshop/pricing/application/service"
  mkd "$base/src/main/java/com/veggieshop/pricing/application/impl"
  mkd "$base/src/main/java/com/veggieshop/pricing/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/pricing/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/pricing/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/pricing/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/pricing/api/http/mapper"
  mkf "$base/pom.xml"

  for f in PriceList.java PriceEntry.java TaxClass.java Promotion.java Coupon.java TierPrice.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/domain/$f"; done

  for f in PriceListRepositoryPort.java PriceEntryRepositoryPort.java PromotionRepositoryPort.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/application/port/$f"; done

  for f in PricingService.java PromotionService.java CouponService.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/application/service/$f"; done

  for f in PricingServiceImpl.java PromotionServiceImpl.java CouponServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/application/impl/$f"; done

  for f in PriceListRepository.java PriceEntryRepository.java PromotionRepository.java CouponRepository.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/infrastructure/persistence/jpa/$f"; done

  for f in PriceListRepositoryAdapter.java PriceEntryRepositoryAdapter.java PromotionRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/infrastructure/adapter/$f"; done

  for f in PricingController.java CouponController.java PromotionController.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/api/http/controller/$f"; done

  for f in PriceListDTO.java PriceEntryDTO.java PromotionDTO.java CouponDTO.java TierPriceDTO.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/api/http/dto/$f"; done

  for f in PriceListMapper.java PriceEntryMapper.java PromotionMapper.java CouponMapper.java
  do mkf "$base/src/main/java/com/veggieshop/pricing/api/http/mapper/$f"; done
}

context_customer () {
  local base="$BASE_DIR/modules/contexts/context-customer"
  mkd "$base/src/main/java/com/veggieshop/customer/domain"
  mkd "$base/src/main/java/com/veggieshop/customer/application/port"
  mkd "$base/src/main/java/com/veggieshop/customer/application/service"
  mkd "$base/src/main/java/com/veggieshop/customer/application/impl"
  mkd "$base/src/main/java/com/veggieshop/customer/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/customer/infrastructure/persistence/adapter"
  mkd "$base/src/main/java/com/veggieshop/customer/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/customer/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/customer/api/http/mapper"
  mkf "$base/pom.xml"

  for f in Customer.java CustomerProfile.java AddressEntity.java LoyaltyAccount.java Wishlist.java WishlistItem.java SavedPaymentMethod.java
  do mkf "$base/src/main/java/com/veggieshop/customer/domain/$f"; done

  for f in CustomerRepositoryPort.java AddressRepositoryPort.java
  do mkf "$base/src/main/java/com/veggieshop/customer/application/port/$f"; done

  for f in CustomerService.java AddressBookService.java
  do mkf "$base/src/main/java/com/veggieshop/customer/application/service/$f"; done

  for f in CustomerServiceImpl.java AddressBookServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/customer/application/impl/$f"; done

  for f in CustomerRepository.java AddressEntityRepository.java LoyaltyAccountRepository.java WishlistRepository.java WishlistItemRepository.java SavedPaymentMethodRepository.java
  do mkf "$base/src/main/java/com/veggieshop/customer/infrastructure/persistence/jpa/$f"; done

  for f in CustomerRepositoryAdapter.java AddressRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/customer/infrastructure/persistence/adapter/$f"; done

  for f in CustomerController.java AddressBookController.java
  do mkf "$base/src/main/java/com/veggieshop/customer/api/http/controller/$f"; done

  for f in CustomerDTO.java CustomerProfileDTO.java AddressEntityDTO.java
  do mkf "$base/src/main/java/com/veggieshop/customer/api/http/dto/$f"; done

  for f in CustomerMapper.java CustomerProfileMapper.java
  do mkf "$base/src/main/java/com/veggieshop/customer/api/http/mapper/$f"; done
}

context_order () {
  local base="$BASE_DIR/modules/contexts/context-order"
  mkd "$base/src/main/java/com/veggieshop/order/domain"
  mkd "$base/src/main/java/com/veggieshop/order/application/port"
  mkd "$base/src/main/java/com/veggieshop/order/application/service"
  mkd "$base/src/main/java/com/veggieshop/order/application/impl"
  mkd "$base/src/main/java/com/veggieshop/order/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/order/infrastructure/persistence/adapter"
  mkd "$base/src/main/java/com/veggieshop/order/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/order/api/http/dto/request"
  mkd "$base/src/main/java/com/veggieshop/order/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/order/api/http/mapper"
  mkf "$base/pom.xml"

  for f in Cart.java CartItem.java Order.java OrderItem.java Shipment.java ShipmentItem.java DeliverySlot.java Payment.java PaymentTransaction.java Invoice.java Refund.java ReturnRequest.java
  do mkf "$base/src/main/java/com/veggieshop/order/domain/$f"; done

  for f in CartRepositoryPort.java OrderRepositoryPort.java PaymentGatewayPort.java
  do mkf "$base/src/main/java/com/veggieshop/order/application/port/$f"; done

  for f in CartService.java OrderService.java PaymentService.java DeliveryService.java
  do mkf "$base/src/main/java/com/veggieshop/order/application/service/$f"; done

  for f in CartServiceImpl.java OrderServiceImpl.java PaymentServiceImpl.java DeliveryServiceImpl.java
  do mkf "$base/src/main/java/com/veggieshop/order/application/impl/$f"; done

  for f in CartRepository.java CartItemRepository.java OrderRepository.java OrderItemRepository.java ShipmentRepository.java ShipmentItemRepository.java DeliverySlotRepository.java PaymentRepository.java PaymentTransactionRepository.java InvoiceRepository.java RefundRepository.java ReturnRequestRepository.java
  do mkf "$base/src/main/java/com/veggieshop/order/infrastructure/persistence/jpa/$f"; done

  for f in CartRepositoryAdapter.java OrderRepositoryAdapter.java PaymentRepositoryAdapter.java
  do mkf "$base/src/main/java/com/veggieshop/order/infrastructure/persistence/adapter/$f"; done

  for f in CartController.java OrderController.java PaymentController.java DeliveryController.java
  do mkf "$base/src/main/java/com/veggieshop/order/api/http/controller/$f"; done

  for f in CartDTO.java CartItemDTO.java OrderDTO.java OrderItemDTO.java ShipmentDTO.java ShipmentItemDTO.java DeliverySlotDTO.java PaymentDTO.java PaymentTransactionDTO.java InvoiceDTO.java RefundDTO.java ReturnRequestDTO.java
  do mkf "$base/src/main/java/com/veggieshop/order/api/http/dto/$f"; done

  for f in AddToCartRequest.java CheckoutRequest.java CreateOrderRequest.java ConfirmPaymentRequest.java BookDeliverySlotRequest.java
  do mkf "$base/src/main/java/com/veggieshop/order/api/http/dto/request/$f"; done

  for f in CartMapper.java CartItemMapper.java OrderMapper.java OrderItemMapper.java ShipmentMapper.java ShipmentItemMapper.java DeliverySlotMapper.java PaymentMapper.java PaymentTransactionMapper.java InvoiceMapper.java RefundMapper.java ReturnRequestMapper.java
  do mkf "$base/src/main/java/com/veggieshop/order/api/http/mapper/$f"; done
}

context_checkout () {
  local base="$BASE_DIR/modules/contexts/context-checkout"
  mkd "$base/src/main/java/com/veggieshop/checkout/domain"
  mkd "$base/src/main/java/com/veggieshop/checkout/application/port"
  mkd "$base/src/main/java/com/veggieshop/checkout/application/service"
  mkd "$base/src/main/java/com/veggieshop/checkout/application/impl"
  mkd "$base/src/main/java/com/veggieshop/checkout/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/checkout/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/checkout/api/http/controller"
  mkd "$base/src/main/java/com/veggieshop/checkout/api/http/dto"
  mkd "$base/src/main/java/com/veggieshop/checkout/api/http/mapper"
  mkf "$base/pom.xml"

  mkf "$base/src/main/java/com/veggieshop/checkout/domain/CheckoutSession.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/application/port/CheckoutSessionRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/application/service/CheckoutService.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/application/impl/CheckoutServiceImpl.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/infrastructure/persistence/jpa/CheckoutSessionRepository.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/infrastructure/adapter/CheckoutSessionRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/api/http/controller/CheckoutController.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/api/http/dto/CheckoutSessionDTO.java"
  mkf "$base/src/main/java/com/veggieshop/checkout/api/http/mapper/CheckoutSessionMapper.java"
}

context_vendor () {
  local base="$BASE_DIR/modules/contexts/context-vendor"
  mkd "$base/src/main/java/com/veggieshop/vendor/domain"
  mkd "$base/src/main/java/com/veggieshop/vendor/application/port"
  mkd "$base/src/main/java/com/veggieshop/vendor/application/service"
  mkd "$base/src/main/java/com/veggieshop/vendor/application/impl"
  mkd "$base/src/main/java/com/veggieshop/vendor/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/vendor/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/vendor/api/http/controller"
  mkf "$base/pom.xml"

  for f in Supplier.java Farm.java HarvestBatch.java ComplianceDocument.java
  do mkf "$base/src/main/java/com/veggieshop/vendor/domain/$f"; done

  mkf "$base/src/main/java/com/veggieshop/vendor/application/port/SupplierRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/vendor/application/service/SupplierService.java"
  mkf "$base/src/main/java/com/veggieshop/vendor/application/impl/SupplierServiceImpl.java"

  for f in SupplierRepository.java FarmRepository.java HarvestBatchRepository.java ComplianceDocumentRepository.java
  do mkf "$base/src/main/java/com/veggieshop/vendor/infrastructure/persistence/jpa/$f"; done

  mkf "$base/src/main/java/com/veggieshop/vendor/infrastructure/adapter/SupplierRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/vendor/api/http/controller/SupplierController.java"
}

context_media () {
  local base="$BASE_DIR/modules/contexts/context-media"
  mkd "$base/src/main/java/com/veggieshop/media/domain"
  mkd "$base/src/main/java/com/veggieshop/media/application/port"
  mkd "$base/src/main/java/com/veggieshop/media/application/service"
  mkd "$base/src/main/java/com/veggieshop/media/application/impl"
  mkd "$base/src/main/java/com/veggieshop/media/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/media/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/media/api/http/controller"
  mkf "$base/pom.xml"

  mkf "$base/src/main/java/com/veggieshop/media/domain/MediaAsset.java"
  mkf "$base/src/main/java/com/veggieshop/media/application/port/MediaAssetRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/media/application/service/MediaService.java"
  mkf "$base/src/main/java/com/veggieshop/media/application/impl/MediaServiceImpl.java"
  mkf "$base/src/main/java/com/veggieshop/media/infrastructure/persistence/jpa/MediaAssetRepository.java"
  mkf "$base/src/main/java/com/veggieshop/media/infrastructure/adapter/MediaAssetRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/media/api/http/controller/MediaController.java"
}

context_review () {
  local base="$BASE_DIR/modules/contexts/context-review"
  mkd "$base/src/main/java/com/veggieshop/review/domain"
  mkd "$base/src/main/java/com/veggieshop/review/application/port"
  mkd "$base/src/main/java/com/veggieshop/review/application/service"
  mkd "$base/src/main/java/com/veggieshop/review/application/impl"
  mkd "$base/src/main/java/com/veggieshop/review/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/review/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/review/api/http/controller"
  mkf "$base/pom.xml"

  mkf "$base/src/main/java/com/veggieshop/review/domain/Review.java"
  mkf "$base/src/main/java/com/veggieshop/review/domain/ReviewFlag.java"
  mkf "$base/src/main/java/com/veggieshop/review/application/port/ReviewRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/review/application/service/ReviewService.java"
  mkf "$base/src/main/java/com/veggieshop/review/application/impl/ReviewServiceImpl.java"
  mkf "$base/src/main/java/com/veggieshop/review/infrastructure/persistence/jpa/ReviewRepository.java"
  mkf "$base/src/main/java/com/veggieshop/review/infrastructure/persistence/jpa/ReviewFlagRepository.java"
  mkf "$base/src/main/java/com/veggieshop/review/infrastructure/adapter/ReviewRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/review/api/http/controller/ReviewController.java"
}

context_notification () {
  local base="$BASE_DIR/modules/contexts/context-notification"
  mkd "$base/src/main/java/com/veggieshop/notification/domain"
  mkd "$base/src/main/java/com/veggieshop/notification/application/port"
  mkd "$base/src/main/java/com/veggieshop/notification/application/service"
  mkd "$base/src/main/java/com/veggieshop/notification/application/impl"
  mkd "$base/src/main/java/com/veggieshop/notification/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/notification/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/notification/api/http/controller"
  mkf "$base/pom.xml"

  mkf "$base/src/main/java/com/veggieshop/notification/domain/Notification.java"
  mkf "$base/src/main/java/com/veggieshop/notification/domain/ChannelPreference.java"
  mkf "$base/src/main/java/com/veggieshop/notification/application/port/NotificationRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/notification/application/port/ChannelPreferenceRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/notification/application/service/NotificationService.java"
  mkf "$base/src/main/java/com/veggieshop/notification/application/impl/NotificationServiceImpl.java"
  mkf "$base/src/main/java/com/veggieshop/notification/infrastructure/persistence/jpa/NotificationRepository.java"
  mkf "$base/src/main/java/com/veggieshop/notification/infrastructure/persistence/jpa/ChannelPreferenceRepository.java"
  mkf "$base/src/main/java/com/veggieshop/notification/infrastructure/adapter/NotificationRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/notification/api/http/controller/NotificationController.java"
}

context_audit () {
  local base="$BASE_DIR/modules/contexts/context-audit"
  mkd "$base/src/main/java/com/veggieshop/audit/domain"
  mkd "$base/src/main/java/com/veggieshop/audit/application/port"
  mkd "$base/src/main/java/com/veggieshop/audit/application/service"
  mkd "$base/src/main/java/com/veggieshop/audit/application/impl"
  mkd "$base/src/main/java/com/veggieshop/audit/infrastructure/persistence/jpa"
  mkd "$base/src/main/java/com/veggieshop/audit/infrastructure/adapter"
  mkd "$base/src/main/java/com/veggieshop/audit/api/http/controller"
  mkf "$base/pom.xml"

  mkf "$base/src/main/java/com/veggieshop/audit/domain/AuditLog.java"
  mkf "$base/src/main/java/com/veggieshop/audit/application/port/AuditLogRepositoryPort.java"
  mkf "$base/src/main/java/com/veggieshop/audit/application/service/AuditService.java"
  mkf "$base/src/main/java/com/veggieshop/audit/application/impl/AuditServiceImpl.java"
  mkf "$base/src/main/java/com/veggieshop/audit/infrastructure/persistence/jpa/AuditLogRepository.java"
  mkf "$base/src/main/java/com/veggieshop/audit/infrastructure/adapter/AuditLogRepositoryAdapter.java"
  mkf "$base/src/main/java/com/veggieshop/audit/api/http/controller/AuditController.java"
}

# Create contexts
log "Creating contexts/*"
context_auth
context_catalog
context_inventory
context_pricing
context_customer
context_order
context_checkout
context_vendor
context_media
context_review
context_notification
context_audit

# --- Apps (entry point) -------------------------------------------------------
log "Creating apps/veggieshop-service"
mkd "$BASE_DIR/apps/veggieshop-service/src/main/java/com/veggieshop/bootstrap"
mkd "$BASE_DIR/apps/veggieshop-service/src/main/resources"
mkf "$BASE_DIR/apps/veggieshop-service/pom.xml"
mkf "$BASE_DIR/apps/veggieshop-service/src/main/java/com/veggieshop/bootstrap/VegetableShopApplication.java"
for f in application.yml application-local.yml application-prod.yml logback-spring.xml
do mkf "$BASE_DIR/apps/veggieshop-service/src/main/resources/$f"; done

# --- tests (awareness) --------------------------------------------------------
log "Creating tests awareness folder"
mkd "$BASE_DIR/tests"
mkf "$BASE_DIR/tests/README.md"

# --- chmod helpful bits on Unix-like shells -----------------------------------
chmod +x "$BASE_DIR/mvnw" 2>/dev/null || true
chmod +x "$BASE_DIR/scripts/"*.sh 2>/dev/null || true

log "Done. Rough counts:"
printf "  files:   %s\n" "$(find "$BASE_DIR" -type f | wc -l | tr -d ' ')"
printf "  dirs:    %s\n" "$(find "$BASE_DIR" -type d | wc -l | tr -d ' ')"
