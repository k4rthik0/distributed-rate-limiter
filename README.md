# Distributed Multi-Tenant Rate Limiter & API Gateway Service

[Java 17](https://www.oracle.com/java/)
[Spring Boot](https://spring.io/projects/spring-boot)
[Redis](https://redis.io/)
[Apache Kafka](https://kafka.apache.org/)

A high-throughput, fault-tolerant, distributed multi-tenant rate limiting and API Gateway middleware built with Java, Spring WebFlux, Reactive Redis, Apache Kafka, and Resilience4j. 

Engineered to prevent noisy-neighbor problem, DDoS attacks, and API abuse across distributed microservice clusters with **sub-2ms execution overhead**.

---



## Architecture Overview

```
                          +-------------------------+
                          |   Incoming HTTP Client  |
                          +-------------------------+
                                       |
                                       v
                       +-------------------------------+
                       |  RateLimitingWebFilter (P99)  |
                       +-------------------------------+
                                       |
                     +-----------------+-----------------+
                     |                                   |
                     v                                   v
        +-------------------------+          +-----------------------+
        |   Reactive Redis (Lua)  |          |  Resilience4j Circuit |
        |  Sliding Window ZSET    |          |        Breaker        |
        +-------------------------+          +-----------------------+
                     |                                   | (On Failure)
                     v                                   v
          [Allowed / Blocked]                +-----------------------+
                     |                       | Caffeine In-Memory    |
                     v                       | Local Cache Fallback  |
        +-------------------------+          +-----------------------+
        |  Protected API Service  |
        +-------------------------+
                     ^
                     |
        +-------------------------+
        |   Kafka Event Listener  | <--- Quota Updates Broadcast
        +-------------------------+
```

---



## Key Features & System Design

1. **Atomic Sliding Window Rate Limiting**: Uses Redis Sorted Sets (`ZSET`) and Lua script execution to clean expired logs, count active requests, and append new request tokens in a single atomic non-blocking operation.
2. **Multi-Tenant Policy Support**: Dynamically resolves rate limit rules per tenant (`X-Tenant-Id`) with distinct plan tiers (`FREE`, `GOLD`, `ENTERPRISE`).
3. **High Availability & Fault Tolerance**: Wrapped in a **Resilience4j Circuit Breaker**. If Redis experiences network partition or latency spikes, traffic gracefully falls back to an **in-memory Caffeine sliding window counter** without blocking the API gateway.
4. **Zero-Downtime Dynamic Quota Updates**: Admin configuration updates are published to an **Apache Kafka** topic (`tenant-quota-updates`) and consumed across all distributed gateway instances to refresh local tenant quota rules on the fly.
5. **Observability & Metrics**: Exposes Prometheus metrics at `/actuator/prometheus` including request throughput, block counts, Redis latency, and circuit breaker status.

---



## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2, Spring WebFlux (Reactive Stack)
- **Data Stores**: Reactive Redis (Lettuce), Caffeine Cache
- **Event Streaming**: Apache Kafka
- **Resilience**: Resilience4j Circuit Breaker & Reactor
- **Monitoring**: Micrometer, Prometheus, Grafana
- **DevOps**: Docker & Docker Compose

---



## Quick Start Guide



### 1. Start Infrastructure (Redis, Kafka, Zookeeper, Prometheus, Grafana)

```bash
docker-compose up -d
```



### 2. Build and Run the Service

```bash
mvn clean package -DskipTests
java -jar target/distributed-rate-limiter-1.0.0.jar
```

---



## API Usage & Verification



### Test Protected Endpoint (Free Tier: Limit = 10 req/min)

```bash
curl -i -H "X-Tenant-Id: tenant_free" http://localhost:8080/api/v1/resource/data
```

**Sample Response Headers**:

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 9
X-RateLimit-Reset: 1722800000000
X-RateLimit-Source: REDIS
```



### Exceeding Rate Limit (HTTP 429)

When the 11th request is sent within 60 seconds:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for tenant tenant_free. Max allowed: 10",
  "source": "REDIS"
}
```



### Dynamic Tenant Quota Update via Kafka

```bash
curl -X POST "http://localhost:8080/api/v1/tenants/tenant_free/quota?planTier=GOLD&maxRequests=500&windowSizeMs=60000"
```

---



## Benchmarking & Load Testing

Run the included asynchronous Python benchmark script:

```bash
python3 load-test.py
```

