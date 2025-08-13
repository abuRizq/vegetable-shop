# 🧪 Unit Tests – VeggieShop Backend

Welcome to the **Unit Test Suite** for the VeggieShop backend!  
This directory is dedicated to well-structured, focused, and isolated tests that guarantee the core logic of each module is reliable, maintainable, and regression-free.

---

## 📦 Directory Structure

```
unit/
│
├── auth/
│   ├── AuthServiceTest.java
│   ├── EmailServiceTest.java
│   ├── RefreshTokenServiceTest.java
│   ├── PasswordResetTokenServiceTest.java
│   └── SessionMapperTest.java
│
├── category/
│   ├── CategoryServiceTest.java
│   └── CategoryMapperTest.java
│
├── product/
│   ├── ProductServiceTest.java
│   └── ProductMapperTest.java
│
├── offer/
│   ├── OfferServiceTest.java
│   └── OfferMapperTest.java
│
├── order/
│   ├── OrderServiceTest.java
│   └── OrderMapperTest.java
│
├── user/
│   ├── UserServiceTest.java
│   └── UserMapperTest.java
│
├── util/
│   └── PriceCalculatorTest.java
│
├── common/
│   └── ApiResponseUtilTest.java
│
└── README.md
```

---

## 🎯 Test Coverage at a Glance

- **auth/** – Authentication logic, JWT, email, password & refresh tokens, session mapping.
- **category/** – Category CRUD operations and mapping.
- **product/** – Product CRUD, filtering, and mapping logic.
- **offer/** – Offers and discount business logic, mapping.
- **order/** – Orders, order items, status management, advanced mapping.
- **user/** – User CRUD, roles, password changes, mapping.
- **util/** – Utility classes (e.g., price calculation).
- **common/** – Shared API response wrappers, error handling.

---

## 🚀 How to Run the Unit Tests

- **With Maven:**
  ```sh
  ./mvnw test
  ```
  or
  ```sh
  mvn test
  ```

- **From Your IDE:**
    - Right-click the `unit` folder or any test class → `Run`.

> All tests use **JUnit 5** and **Mockito** for mocking.  
> No external infrastructure (DB, SMTP, etc.) is required — tests are fully isolated.  
> Test data and fixtures (if needed) are in `/src/test/resources/`.

---

## 📚 Best Practices

- **Give each test method a clear, expressive name** that describes the scenario and expected result.
- **Mock external dependencies** (repositories, email, etc.) to focus on business logic.
- Always cover **edge cases, validation, and exception scenarios**.
- When changing or adding logic, always update/add the corresponding unit tests.
- For higher-level (integration or E2E) tests, see `integration/` and `e2e/` directories.

---

## 🤝 Contributing

- Keep unit tests up-to-date with code changes.
- Aim for **meaningful, intention-revealing tests** (not just coverage for coverage's sake).
- Contributions and improvements to test quality are always welcome!

---

> _"Well-written unit tests document intent and empower fearless refactoring."_

**Happy Testing!** 🧑‍💻🚦
