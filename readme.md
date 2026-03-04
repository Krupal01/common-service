Here is your **clean README.md (minimal, production-style)** for `dto` package.

No extra explanation. Only purpose, usage, and JSON format.

---

# DTO Package

```
com.krunish.common.dto
 ├── ApiResponse
 ├── ApiError
 └── ApiPage
```

Provides standard API response structure for all services.

---

# 1️⃣ ApiResponse<T>

## Purpose

Wrap all successful API responses.

## JSON Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

## Usage (Controller)

```java
@GetMapping("/{id}")
public ApiResponse<UserDto> get(@PathVariable UUID id) {
    return ApiResponse.success(userService.get(id));
}
```

---

# 2️⃣ ApiError

## Purpose

Standard error response.

Returned automatically by `GlobalExceptionHandler`.

## JSON Format

```json
{
  "success": false,
  "message": "USER_NOT_FOUND",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2026-02-28T10:00:00"
}
```

## Usage (Service)

```java
throw new AppException("USER_NOT_FOUND");
```

Do NOT return manually from controller.

---

# 3️⃣ ApiPage<T>

## Purpose

Standard pagination wrapper.

## JSON Format

```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

## Usage (Controller)

```java
@GetMapping
public ApiResponse<ApiPage<UserDto>> getAll(Pageable pageable) {
    Page<User> page = userService.getAll(pageable);
    return ApiResponse.success(ApiPage.from(page));
}
```

---

# Controller Rules

* Always return `ApiResponse<T>`
* Never return Entity directly
* Never return Page directly
* Never build error JSON manually

---

# Standard Response Pattern

### Success

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

### Error

```json
{
  "success": false,
  "message": "...",
  "errorCode": "...",
  "timestamp": "..."
}
```
Here is the corrected **minimal README.md** for `event` package
(using proper examples like `Product`, not `Customer`).

---

# Event Package

```text
com.krunish.common.event
 ├── BaseOrgEntity
 ├── KafkaEventEnvelope
 └── OrgIdFilter
```

Provides:

* Multi-tenant entity base
* Automatic org-level DB filtering
* Standard Kafka event wrapper

---

# 1️⃣ BaseOrgEntity

## Purpose

Base class for all **organization-scoped business entities**.

Adds:

```java
private UUID orgId;
```

Ensures data belongs to a specific organization.

---

## Usage (Entity)

```java
@Entity
public class Product extends BaseOrgEntity {
}
```

Use in:

* Product
* Order
* Invoice
* Subscription
* Any org-owned business data

Do NOT use in:

* User (global)
* Organization
* UserOrganization (mapping table)

---

## Result

All queries automatically scoped by `orgId`.

---

# 2️⃣ OrgIdFilter

## Purpose

Hibernate filter for automatic tenant isolation.

Applies automatically:

```sql
WHERE org_id = :orgId
```

Prevents cross-organization data access.

---

## Usage

Declared on `BaseOrgEntity`:

```java
@Filter(name = "orgFilter", condition = "org_id = :orgId")
```

Enabled per request via security layer.

No manual filtering required in repositories.

---

## Flow

```
Request
 ↓
OrgContext set
 ↓
Hibernate filter enabled
 ↓
All queries auto-filtered by org_id
```

---

# 3️⃣ KafkaEventEnvelope<T>

## Purpose

Standard wrapper for Kafka messages.

Ensures events are tenant-aware.

---

## JSON Format

```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_CREATED",
  "orgId": "uuid",
  "timestamp": "2026-02-28T10:00:00",
  "payload": {}
}
```

---

## Usage (Producer)

```java
KafkaEventEnvelope<ProductDto> event =
    new KafkaEventEnvelope<>("PRODUCT_CREATED", orgId, productDto);

kafkaTemplate.send("product-topic", event);
```

---

## Usage (Consumer)

```java
@KafkaListener(topics = "product-topic")
public void consume(KafkaEventEnvelope<ProductDto> event) {

    UUID orgId = event.getOrgId();
    ProductDto payload = event.getPayload();
}
```

---

# Rules

* All org-owned entities must extend `BaseOrgEntity`
* Never manually add `WHERE org_id` in repository
* Always wrap Kafka messages in `KafkaEventEnvelope`
* Do not publish raw payload directly

---

# Standard Pattern

## Database

```
Product extends BaseOrgEntity
 ↓
OrgIdFilter
 ↓
Automatic org isolation
```

## Kafka

```
ProductDto
 ↓
KafkaEventEnvelope
 ↓
Publish
```
Good point ✅ — README should clearly show **where each class is used**.

Here is the corrected short version with explicit usage of both classes.

---

# Exception Package

```text
com.krunish.common.exception
 ├── AppException
 └── GlobalExceptionHandler
```

Provides centralized business error handling.

---

# 1️⃣ AppException

## Purpose

Used to throw business-level errors from Service layer.

## Where To Use

Use inside:

* Service classes
* Validation logic
* Security checks

## Example

```java
if (product == null) {
    throw new AppException("PRODUCT_NOT_FOUND");
}
```

Do NOT use in controllers for response building.

---

# 2️⃣ GlobalExceptionHandler

## Purpose

Automatically catches exceptions and converts them to standard API error response.

## Where It Is Used

* Registered globally using `@RestControllerAdvice`
* Intercepts all exceptions thrown in controllers/services

No manual usage required.

---

# Flow

```text
Service throws AppException
        ↓
GlobalExceptionHandler catches it
        ↓
Returns ApiError JSON
```

---

# Standard Error JSON

```json
{
  "success": false,
  "message": "PRODUCT_NOT_FOUND",
  "errorCode": "PRODUCT_NOT_FOUND",
  "timestamp": "2026-02-28T10:00:00"
}
```

---

# Rules

* Throw `AppException` in Service layer
* Do not catch it in Controller
* Do not manually build error response
* Let `GlobalExceptionHandler` handle everything
  Here is the **concise, implementation-focused README.md** for your `security` package.

No unnecessary theory. Clear purpose, usage, and implementation guidance.

---

# Security Package

```text
com.krunish.common.security
 ├── aop
 ├── AuthProperties
 ├── AuthSecurityProperties
 ├── AuthUser
 ├── AuthWrapper
 ├── HibernateFilterConfigurer
 ├── JwtFilter
 ├── JwtValidator
 ├── OrgAccessValidator
 └── OrgContext
```

Provides:

* JWT authentication
* Multi-tenant org validation
* Automatic org DB filtering
* Centralized security configuration

---

# 🔐 Security Flow

```text
Request
 ↓
JwtFilter
 ↓
JwtValidator
 ↓
OrgAccessValidator
 ↓
OrgContext.set()
 ↓
HibernateFilterConfigurer
 ↓
Controller
```

---

# 1️⃣ AuthUser

## Purpose

Represents authenticated user extracted from JWT.

## Contains

* userId
* email
* roles / permissions

## Used By

* JwtValidator (returns it)
* OrgContext (stores it)

---

# 2️⃣ JwtValidator (Interface)

## Purpose

Validates JWT and extracts user info.

## You Must Implement

```java
@Component
public class JwtValidatorImpl implements JwtValidator {

    @Override
    public AuthUser validate(String token) {
        // validate signature
        // parse claims
        return new AuthUser(...);
    }
}
```

## Used By

* JwtFilter

---

# 3️⃣ OrgAccessValidator (Interface)

## Purpose

Checks if user belongs to selected organization.

## You Must Implement

```java
@Component
public class OrgAccessValidatorImpl implements OrgAccessValidator {

    @Override
    public void validate(UUID userId, UUID orgId) {
        boolean allowed = userOrgRepository
            .existsByUserIdAndOrgId(userId, orgId);

        if (!allowed) {
            throw new AppException("ORG_ACCESS_DENIED");
        }
    }
}
```

## Used By

* JwtFilter

---

# 4️⃣ OrgContext

## Purpose

Stores current request context using ThreadLocal.

## Stores

* userId
* orgId
* AuthUser

## Usage Anywhere

```java
UUID userId = OrgContext.getUserId();
UUID orgId = OrgContext.getOrgId();
```

Automatically cleared after request.

---

# 5️⃣ JwtFilter

## Purpose

Main security filter (runs once per request).

## Responsibilities

* Extract token
* Validate token (JwtValidator)
* Read `X-ORG-ID`
* Validate org access (OrgAccessValidator)
* Set OrgContext

No controller-level security required.

---

# 6️⃣ HibernateFilterConfigurer

## Purpose

Enables Hibernate org filter per request.

Automatically applies:

```sql
WHERE org_id = :orgId
```

Uses value from:

```
OrgContext.getOrgId()
```

No manual filtering needed in repositories.

---

# 7️⃣ AuthWrapper

## Purpose

Registers and configures:

* JwtFilter
* Security configuration
* Public paths

Auto-configures security for service.

No need to write custom Spring Security config in each service.

---

# 8️⃣ AuthProperties

## Purpose

Defines public (non-authenticated) endpoints.

## application.yml

```yaml
auth:
  public-paths:
    - /api/auth/**
    - /actuator/**
```

Used by JwtFilter to skip authentication.

---

# 9️⃣ AuthSecurityProperties

## Purpose

Advanced security config properties.

Examples:

* Header names
* Token prefix
* Org header name

Configured via `application.yml`.

---

# 🔟 aop (Permission Layer)

Used for method-level permission checks.

Example:

```java
@RequiresPermission("PRODUCT_CREATE")
public void createProduct() { }
```

Handled via Aspect class.

---

# How To Use In A Service

## Step 1 – Add dependency
```xml
<dependency>
    <groupId>com.krunish</groupId>
    <artifactId>common</artifactId>
</dependency>
```
---

## Step 2 – Configure public endpoints

```yaml
auth:
  public-paths:
    - /api/auth/**
```

---

## Step 3 – Implement Required Interfaces

You must implement:

* JwtValidator
* OrgAccessValidator

Everything else works automatically.

---

# What Developers Should NOT Do

* Do NOT validate JWT in controller
* Do NOT manually check org in controller
* Do NOT manually filter by org in repository
* Do NOT parse token manually

Everything handled automatically.

---

# Final Responsibility Table

| Class                     | Responsibility              |
| ------------------------- | --------------------------- |
| JwtFilter                 | Entry security filter       |
| JwtValidator              | Validate JWT                |
| OrgAccessValidator        | Validate user-org relation  |
| OrgContext                | Store request context       |
| HibernateFilterConfigurer | Apply DB org filter         |
| AuthWrapper               | Auto security configuration |
| AuthProperties            | Public path config          |
| AuthSecurityProperties    | Header/token config         |
| AuthUser                  | Authenticated user model    |

---

This package provides:

* Centralized authentication
* Multi-tenant enforcement
* Automatic DB isolation
* Clean controllers
* No security duplication across services
