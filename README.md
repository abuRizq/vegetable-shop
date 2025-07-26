# 🥕 Vegetable Shop – Fullstack E-commerce App

A modern, containerized web application for buying and managing fresh vegetables. Built using **Java Spring Boot**, **Next.js**, **PostgreSQL**, and **Docker**.

---

## 📦 Tech Stack

| Layer    | Technology                        |
| -------- | --------------------------------- |
| Frontend | Next.js 15 (React 19, TypeScript) |
| Backend  | Spring Boot 3.3.0 (Java 17)       |
| Database | PostgreSQL 15                     |
| DevOps   | Docker, Docker Compose            |

---

## 🚀 Quick Start (Docker Compose)

### 1. Clone the repository

```bash
git clone https://github.com/your-org/vegetable-shop.git
cd vegetable-shop
```

### 2. Run the app with Docker Compose

```bash
docker-compose up --build
```

### 3. Access the app

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api

> ✅ Make sure ports 3000 and 8080 are available.

---

## ⚙️ Environment Variables

### 📁 backend/src/main/resources/application.yml

```yaml
DB_URL: jdbc:postgresql://localhost:5432/vegetable_shop
DB_USER: postgres
DB_PASS: secret
ADMIN_USER: admin
ADMIN_PASS: admin123
JWT_SECRET: MySuperSecretKey
JWT_EXPIRATION: 86400000
```

### 📁 frontend/.env.local

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## 🧪 Project Structure

```
vegetable-shop/
├── backend/       → Spring Boot app
├── frontend/      → Next.js frontend
├── docker-compose.yml
├── README.md
```

---

## 🔐 Admin Credentials

```
Username: admin
Password: admin123
```

---

## 📚 Documentation

Full documentation with architecture, ERD, API, and deployment guide:
[Open vegetable_shop.html](./docs/vegetable_shop.html)

---

## ❓ Troubleshooting

- PostgreSQL must be running and accepting connections
- If frontend can't access API, check:
  - CORS settings
  - NEXT_PUBLIC_API_URL
  - Docker ports

---

## 📘 API Reference

| Entity      | Endpoint           | Methods                               |
| ----------- | ------------------ | ------------------------------------- |
| Auth        | `/api/auth/login`  | POST                                  |
| Users       | `/api/users`       | GET, POST (Admin), PUT/PATCH, DELETE  |
| Products    | `/api/products`    | GET, POST (Admin), PUT/PATCH, DELETE  |
| Categories  | `/api/categories`  | GET, POST (Admin), PUT/PATCH, DELETE  |
| Offers      | `/api/offers`      | GET, POST (Admin), PUT/PATCH, DELETE  |
| Orders      | `/api/orders`      | GET, POST, PUT/PATCH (status), DELETE |
| Order Items | `/api/order-items` | GET (Admin), POST (via order), DELETE |

📎 See Swagger UI for full request/response structure.

## ✅ Optional: Unified Dockerfile?

This project uses separate Dockerfiles for frontend and backend, managed via `docker-compose.yml`.  
You do **not** need a root-level Dockerfile unless you want a **single image** for both.

---

## 📜 License

MIT © 2025 VegetableShop Team
