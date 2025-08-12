# 🥗 VeggieShop Integration & Auth Test Suite

Welcome to the **VeggieShop Integration and Authentication Test Suite**!
This folder contains comprehensive, production-grade tests for every major API module in your backend — including authentication, authorization, users, orders, categories, offers, and more.

---

## 📂 Directory Overview

```
integration/
│
├── AuthIntegrationTest.java        # End-to-end tests for all auth/session endpoints
├── CategoryIntegrationTest.java    # CRUD + search tests for product categories
├── OfferIntegrationTest.java       # Admin/user flows for offer creation and access
├── OrderIntegrationTest.java       # Robust API tests for order lifecycle and role restrictions
├── SecurityIntegrationTest.java    # Security config & public/private access checks
├── UserIntegrationTest.java        # User CRUD, search, role & password management
└── README.md                       # (You are here)
```

---

## ✅ What’s Covered?

* **Authentication & Session:**

    * Register, login, JWT/refresh, session management
    * Logout and token revocation, edge cases (duplicate/invalid)
* **User Management:**

    * Create, read, update, delete (CRUD) users
    * Change password, change role, email conflict, validation errors
* **Category API:**

    * Category create/update/delete (CRUD) for admins
    * User/role-based access restrictions
    * Search, validation, unique constraints
* **Offer API:**

    * Offers CRUD for admins; list/search for users
    * Validation, non-existent/invalid product
    * Access rules: only admins create/delete
* **Order API:**

    * Users can create and view their own orders
    * Admins see all orders and change status
    * Forbid users from seeing others' orders or admin-only actions
* **Security Layer:**

    * Public vs protected endpoint checks
    * Swagger/docs open
    * Forbidden/unauthenticated flows

---

## 🚀 Running the Tests

**With Maven:**

```sh
./mvnw test
```

You may also filter by test class if needed:

```sh
./mvnw test -Dtest=AuthIntegrationTest,CategoryIntegrationTest
```

**From Your IDE:**

* Right-click the `integration` folder or any test class > `Run`/`Debug`.

> All tests are built with **JUnit 5** and Spring Boot Test tools (MockMvc, @SpringBootTest, @AutoConfigureMockMvc).

---

## 🧑‍💻 Best Practices in These Tests

* **Real API Simulation:** Use MockMvc to hit real HTTP endpoints and verify actual behavior.
* **Role & Security Checks:** Every restricted endpoint is tested for both success (allowed) and failure (forbidden/unauthenticated).
* **Minimal, Isolated Data:** Each test runs with minimal required data, often using unique or test-only emails/names.
* **Positive & Negative Paths:** Both valid (happy path) and invalid/failure scenarios are covered.
* **No External Side Effects:** No real emails sent, no external dependencies; all in-memory or test DB only.
* **Comprehensive Assertions:** Status codes, error messages, and DB side-effects are all checked.

---

## 🔗 Related Test Directories

* `unit/` — Low-level business logic and service layer tests
* `integration/` — These API-level, end-to-end tests
* `security/` — If present, deeper unit tests for security beans/filters (see security/README.md)

---

> *"Integration tests are your contract with frontend clients and partners. Treat them as living documentation, not just bug nets!"*

**Test confidently. Test what matters!** 🚦
