# Endpoints → Controllers (Best-Effort Mapping)

> This table maps common endpoint prefixes to controller classes found in the repository structure. Exact paths may differ; consult OpenAPI specs and `@RequestMapping` annotations for authoritative routes.

| Area | Endpoint Prefix | Controller Class (package) |
|------|------------------|-----------------------------|
| Auth | `/api/auth/*` | `com.veggieshop.auth.api.http.controllers.AuthController` |
| Auth | `/api/auth/*` | `...PasswordController`, `...VerificationController`, `...MfaController`, `...OAuthController`, `...SessionController` |
| Catalog | `/api/catalog/*` | `com.veggieshop.catalog.api.http.controller.ProductController`, `CategoryController`, `TagController` |
| Inventory | `/api/inventory/*` | `com.veggieshop.inventory.api.http.controller.InventoryController`, `StockReservationController`, `WarehouseController` |
| Pricing | `/api/pricing/*` | `com.veggieshop.pricing.api.http.controller.PricingController`, `PromotionController`, `CouponController` |
| Customer | `/api/customer/*` | `com.veggieshop.customer.api.http.controller.CustomerController`, `AddressBookController` |
| Order | `/api/order/*` | `com.veggieshop.order.api.http.controller.CartController`, `OrderController`, `PaymentController`, `DeliveryController` |
| Checkout | `/api/checkout/*` | `com.veggieshop.checkout.api.http.controller.CheckoutController` |
| Vendor | `/api/vendor/*` | `com.veggieshop.vendor.api.http.controller.SupplierController` |
| Media | `/api/media/*` | `com.veggieshop.media.api.http.controller.MediaController` |
| Review | `/api/review/*` | `com.veggieshop.review.api.http.controller.ReviewController` |
| Notification | `/api/notification/*` | `com.veggieshop.notification.api.http.controller.NotificationController` |
| Audit | `/api/audit/*` | `com.veggieshop.audit.api.http.controller.AuditController` |

