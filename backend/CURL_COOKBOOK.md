# VeggieShop cURL Cookbook (Best-Effort)

Base URL (local): `http://localhost:8080`  
If an endpoint requires auth, include: `-H "Authorization: Bearer <JWT_TOKEN>"`

> Note: exact payloads/paths may vary based on your OpenAPI specs and controller mappings.

---

## Auth
```bash
# Register
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Str0ngP@ss!","fullName":"Jane Doe"}'

# Login
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"Str0ngP@ss!"}'

# Refresh token
curl -i -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'
```

## Catalog
```bash
# List products
curl -i http://localhost:8080/api/catalog/products?page=0&size=20

# Create product
curl -i -X POST http://localhost:8080/api/catalog/products \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Organic Tomato","sku":"SKU-123","price":{"amount":2.99,"currency":"USD"}}'

# Get categories tree
curl -i http://localhost:8080/api/catalog/categories/tree
```

## Inventory
```bash
# List warehouses
curl -i http://localhost:8080/api/inventory/warehouses

# Reserve stock
curl -i -X POST http://localhost:8080/api/inventory/stock-reservations \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-123","quantity":3,"warehouseId":"WH-1"}'
```

## Pricing
```bash
# Get product price by SKU
curl -i "http://localhost:8080/api/pricing/prices?sku=SKU-123&qty=1"

# Create promotion
curl -i -X POST http://localhost:8080/api/pricing/promotions \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Spring Sale","discountPercent":10,"startDate":"2025-03-01","endDate":"2025-03-31"}'
```

## Customer
```bash
# Get current profile
curl -i http://localhost:8080/api/customer/me -H "Authorization: Bearer <JWT_TOKEN>"

# Add address
curl -i -X POST http://localhost:8080/api/customer/addresses \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"line1":"123 Main St","city":"Gaza","country":"PS","zip":"970"}'
```

## Order & Checkout
```bash
# Create cart
curl -i -X POST http://localhost:8080/api/order/carts -H "Authorization: Bearer <JWT_TOKEN>"

# Add item to cart
curl -i -X POST http://localhost:8080/api/order/carts/CART_ID/items \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-123","quantity":2}'

# Start checkout session
curl -i -X POST http://localhost:8080/api/checkout/sessions \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"cartId":"CART_ID","paymentMethod":"COD","deliverySlot":"2025-08-22T10:00:00Z"}'
```

## Vendor
```bash
# List suppliers
curl -i http://localhost:8080/api/vendor/suppliers
```

## Media
```bash
# Upload media asset (multipart)
curl -i -X POST http://localhost:8080/api/media/assets \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -F "file=@/path/to/image.jpg" \
  -F "alt=Fresh tomatoes"
```

## Review
```bash
# Create review
curl -i -X POST http://localhost:8080/api/review/reviews \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD_ID","rating":5,"comment":"Excellent quality!"}'
```

## Notification
```bash
# Request notification
curl -i -X POST http://localhost:8080/api/notification/notifications \
  -H "Content-Type: application/json" \
  -d '{"type":"EMAIL","recipient":"user@example.com","template":"ORDER_CONFIRMED","data":{"orderId":"ORD-1"}}'
```

## Audit
```bash
# Query audit logs
curl -i "http://localhost:8080/api/audit/logs?from=2025-08-01T00:00:00Z&to=2025-08-31T23:59:59Z&limit=50"
```

