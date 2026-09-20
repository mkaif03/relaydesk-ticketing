# Architectural Decisions and Tradeoffs

This document outlines key architectural decisions and tradeoffs made during the development of RelayDesk.

## 1. Authentication Storage: httpOnly Cookies vs localStorage
- **Decision:** Store JWTs in `httpOnly` cookies named `rd_at` and `rd_rt`.
- **Tradeoff:** Storing tokens in `httpOnly` cookies mitigates XSS (Cross-Site Scripting) attacks since the token cannot be accessed via JavaScript. When adopting Next.js Server Components, we encountered the challenge of cookie-forwarding: since Server Components run on the Node.js server, they do not automatically forward the client's cookies to the Spring Boot API. We had to explicitly read the `rd_at` cookie from Next.js `cookies()` and manually attach it to outgoing `fetch` requests to authenticate API calls from the Server Components.

## 2. Locking Mechanism: Optimistic Locking vs Pessimistic Locking
- **Decision:** Used Optimistic Locking (via JPA `@Version` and a manual `version` field) for `ChangeRequest` and `Release` updates.
- **Tradeoff:** Optimistic locking prevents lost updates when multiple users attempt to edit or review the same CR simultaneously. Before mutating an entity, the API verifies the client's version against the database version and throws a 409 Conflict (`StaleVersionException`) if there's a mismatch. This avoids database-level locking overhead and deadlocks, prioritizing read performance and system scalability.

## 3. Frontend Architecture: App Router Data Fetching vs BFF (Backend-for-Frontend)
- **Decision:** Direct Server Component data fetching to the Spring Boot API, protected by Next.js Middleware.
- **Tradeoff:** Next.js Server Components essentially act as a proxy, fetching data from the Spring Boot API on the server before sending HTML to the client. This avoids the overhead of maintaining a separate GraphQL or Node.js BFF layer. We adopted this pattern and pushed interactive pieces (forms, buttons) to client component leaves using the `"use client"` directive. The primary challenge was properly passing the JWT `rd_at` cookie along to the Spring API on each fetch call within the Server Components.

## 4. Transaction Boundaries: Service-level vs Controller-level `@Transactional`
- **Decision:** Placed `@Transactional` annotations on the Service layer rather than the Controller layer.
- **Tradeoff:** This ensures transactions are kept as short as possible and are tightly scoped to the business logic, reducing database lock contention. However, it requires careful handling of lazy-loaded collections (e.g., `ChangeRequest.reviews`), as they cannot be initialized outside the transaction scope (which led to a `LazyInitializationException` early in development).

## 5. File Storage: Dropped for Scope
- **Decision:** Excluded the File Attachments feature.
- **Tradeoff:** We prioritized delivering a complete, robust core workflow (RBAC, Audit Logging, State Machine) over implementing file attachments. Implementing attachments would have required adding S3 integration or local disk storage handling, complicating the infrastructure and delaying the core release.

## 6. Testing Strategy: Testcontainers vs H2 In-Memory Database
- **Decision:** Used Testcontainers with a real PostgreSQL image for integration tests.
- **Tradeoff:** Testing against a real PostgreSQL instance ensures our tests accurately reflect production behavior, avoiding subtle syntax or dialect differences between H2 and Postgres. The downside is that tests take slightly longer to boot up because a Docker container must be spun up during the test phase.
