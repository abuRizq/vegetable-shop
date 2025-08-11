# 🔒 Security & Auth Tests – VeggieShop Backend

Welcome to the **Security and Authentication Test Suite** for the VeggieShop backend!
This directory is dedicated to well-structured tests that verify all authentication, authorization, and security integration points for your Spring Boot API.

---

## 📁 Directory Structure

```
security/
│
├── AuthEndpointsSecurityTest.java       # Integration tests for authentication and security endpoints
├── JwtAuthFilterTest.java               # Unit tests for the JWT filter logic
├── JwtUtilTest.java                     # Unit tests for JWT generation, parsing, and validation
├── SecurityConfigTest.java              # Bean presence/configuration for security layer
├── UserDetailsServiceImplTest.java      # Unit tests for loading user details & authorities
└── README.md                            # (This file)
```

---

## 🛡️ What Do These Tests Cover?

* **Authentication Endpoints:**

    * Registration, login, and password reset API tests.
    * Duplicate and invalid input cases.
    * Login and logout flows.

* **Authorization & Role-based Access:**

    * Ensures protected endpoints require authentication.
    * Verifies only admins can access admin APIs.
    * Ensures forbidden actions are blocked for normal users.

* **JWT Logic:**

    * Token creation, parsing, expiration, and validation.
    * Rejection of invalid, expired, or malformed tokens.
    * Security context population for valid JWTs.

* **Security Beans & Config:**

    * Checks that essential security beans (filters, encoders, chains) are present and functional.

* **User Details Loading:**

    * Loads users by email, wraps them in security principal, handles not-found and status cases.

---

## 🚀 How to Run the Security Tests

* **With Maven:**

  ```sh
  ./mvnw test -Dtest='*Security*Test,*Jwt*Test,UserDetailsServiceImplTest'
  ```

  or simply

  ```sh
  mvn test
  ```

  (all tests will run if no filters applied)

* **From Your IDE:**

    * Right-click the `security` folder or any test class → `Run`.

> All tests use **JUnit 5** and (where needed) **Mockito** for mocking.
> These tests are a mix of isolated unit tests and lightweight integration tests (using MockMvc & SpringBootTest).

---

## 💡 Best Practices for Security Tests

* **Separation of Concerns:**

    * Unit tests for core logic, integration tests for endpoint access.
* **Clear Naming:**

    * Name tests to reveal scenario & expected outcome.
* **Minimal Data, No External Dependencies:**

    * Use in-memory/mock data. Never hit real services or send real emails.
* **Test Positive & Negative Paths:**

    * Always test what should be allowed *and* what must be blocked.
* **Keep Tests Reliable:**

    * Reset security context and mocks before each test.

---

## 🔗 Related Test Suites

* **unit/** — Business logic unit tests (services, mappers, etc)
* **integration/** — Full-stack API and workflow tests

---

> *"Security tests aren’t just a safety net — they document your contract with clients and protect you from regressions, privilege escalation, and mistakes under pressure."*

**Test smart. Test secure!** 🧑‍💻🛡️
