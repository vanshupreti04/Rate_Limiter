# 🚦 Rate Limiter Service (Token Bucket Algorithm)

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-Reactive-green" />
  <img src="https://img.shields.io/badge/WebFlux-Non--Blocking-brightgreen" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-blue" />
</p>

---

## ✨ Overview

This project is a **Rate Limiter** built using the **Token Bucket Algorithm** to efficiently manage and distribute incoming traffic.
It helps **reduce system load**, **prevent abuse**, and **ensure fair usage** across services by controlling request rates at the gateway level.

> ⚡ Designed for **scalable microservices**, this rate limiter works as a **protective gateway** that validates API keys and enforces rate limits before forwarding traffic to downstream services.

---

## 🧠 How It Works (Token Bucket)

* Each client/service is assigned a **token bucket**
* Tokens are added at a fixed rate
* Every request consumes one token
* If no tokens are available → request is **rate-limited (429 Too Many Requests)**

✔ Smooth traffic handling
✔ Allows short bursts
✔ Prevents overload

---

## 🏗️ Architecture

```
Client
   ↓
API Gateway (Rate Limiter)
   ↓
Token Bucket Validation
   ↓
Target Microservice
```

* API Key–based service identification
* Reactive & non-blocking processing
* Centralized traffic control

---

## 🔐 API Key & Service Registration

Each service must be registered before using the rate limiter.

### 🔑 Service Registration

* Services are registered with a **unique API Key**
* Rate limits are configured per service
* API Key is required in request headers

```http
X-API-KEY: your-api-key-here
```

🚀 Only authorized services can access protected endpoints.

---

## 🛠️ Tech Stack

| Technology           | Usage                        |
| -------------------- | ---------------------------- |
| ☕ **Java**           | Core language                |
| 🌱 **Spring Boot**   | Application framework        |
| ⚡ **Spring WebFlux** | Reactive & non-blocking APIs |
| 🐘 **PostgreSQL**    | Persistent storage           |
| 🔐 **API Key Auth**  | Service-level access control |
| 🚦 **Token Bucket**  | Rate limiting algorithm      |

---

## 📦 Features

✅ Token Bucket–based rate limiting
✅ Reactive & non-blocking (WebFlux)
✅ API Gateway behavior
✅ API Key validation
✅ Service-based traffic isolation
✅ PostgreSQL-backed persistence

---

## 📡 Example Response

### ✅ Allowed Request

```json
{
  "status": "SUCCESS",
  "message": "Request allowed"
}
```

### ❌ Rate Limited

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded"
}
```

---

## 🚀 Use Cases

* Microservices traffic control
* API Gateway protection
* Preventing DDoS & abuse
* Fair usage enforcement

---

## 📂 Project Setup

```bash
git clone https://github.com/your-username/rate-limiter.git
cd rate-limiter
./mvnw spring-boot:run
```

---

## 🔮 Future Enhancements

* Redis-based distributed token bucket
* Dynamic rate limit updates
* Dashboard for monitoring usage
* Multi-tenant support

---

## 👨‍💻 Author

**Vansh Upreti**
🚀 Backend Developer | Java | Spring Boot

---

⭐ If you like this project, don’t forget to **star the repository**!

