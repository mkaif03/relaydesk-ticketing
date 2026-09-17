# AI Usage & Development Log

This document details the usage of AI assistants during the development of RelayDesk.

## Tools Used
- **Google Antigravity IDE (Agentic Coding Assistant)**
- **Spring Boot Ecosystem** (Java 21)
- **Next.js 15 (App Router)**
- **Tailwind CSS & Lucide Icons**

## Key Prompts That Shaped the Design
1. *"Can you list the roles designed and their authorization level in the application?"* - Sparked the full RBAC matrix definition before writing any code.
2. *"Go ahead and implement Authorization with the authorization levels that you mentioned here"* - Resulted in the backend-wide enforcement of `ROLE_ADMIN`, `ROLE_REVIEWER`, `ROLE_ENGINEER`, and `ROLE_RELEASE_MANAGER`.
3. *"ROLE_REVIEWER should be allowed to create Change Requests, not ROLE_RELEASE_MANAGER"* - Corrected the RBAC mapping in `ChangeRequestService.java`.
4. *"Let's implement a small new feature and then move on to documentation. Develop an Audit log UI for the admin"* - Initiated the creation of the `AuditController` and the `admin/audit/page.tsx` UI.
5. *"Yes, include pagination and advanced filtering in the UI"* - Shifted the audit log implementation from a simple array return to utilizing Spring Data `JpaSpecificationExecutor` and `Pageable`.
6. *"Is our current workflow of a Change Request this : DRAFT → SUBMITTED → CHANGES_REQUESTED → SUBMITTED → APPROVED → SCHEDULED → SHIPPED ?"* - Verified the actual state machine implementation vs the user's mental model, ensuring terminology mapping was documented.

## Accepted vs Rewritten Files
- **Accepted Almost Unchanged:**
  - The initial `Dockerfile` and `docker-compose.yml` for the Next.js and Spring Boot setup.
  - The `AuditController.java` and `AuditEventDto.java` mapping layer.
  - Basic `pom.xml` configurations.
- **Heavily Rewritten / Iterated:**
  - `ChangeRequestStateMachine.java`: Initially very simple, but required multiple passes to add role-based guards and specific business logic (e.g. requiring at least one "APPROVED" review and zero "REJECTED" reviews to transition to `APPROVED`).
  - `ChangeRequestService.java`: Modified multiple times to add security checks, transaction boundaries, and audit logging side-effects.

## Hallucinations & Corrections
- **The `LazyInitializationException` Hallucination:** During the development of the CR status update workflow, the AI initially attempted to access a lazy-loaded collection (`ChangeRequest.reviews`) outside of a database transaction in the controller layer. The system threw a `LazyInitializationException`. The AI caught this hallucination, analyzed the stack trace, and corrected the code by moving the transaction boundary to the service layer and ensuring `@Transactional(readOnly = true)` was applied to read operations that required fetching lazy relationships.
