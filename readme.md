# Common Library — README

---

# Package Overview

```text
com.krunish.common
 ├── config
 │    ├── CommonAutoConfiguration          (legacy mode)
 │    └── GenericCommonAutoConfiguration   (generic mode)
 ├── dto
 │    ├── ApiResponse
 │    ├── ApiError
 │    └── ApiPage
 ├── event
 │    ├── BaseOrgEntity
 │    ├── KafkaEventEnvelope
 │    └── OrgIdFilter
 ├── exception
 │    ├── AppException
 │    └── GlobalExceptionHandler
 └── security
      ├── aop
      ├── AuthProperties
      ├── AuthSecurityProperties
      ├── HibernateFilterConfigurer
      ├── [legacy]  AuthUser
      ├── [legacy]  JwtValidator
      ├── [legacy]  JwtFilter
      ├── [legacy]  OrgAccessValidator
      ├── [legacy]  OrgContext
      └── generic
           ├── AuthClaims
           ├── AuthContext
           ├── AuthFilter
           ├── ClaimsExtractor
           ├── DefaultAuthClaims
           ├── DefaultClaimsExtractor
           ├── GenericJwtFilter
           ├── GenericJwtTokenValidator
           ├── GenericOrgAccessValidator
           └── TokenValidator
```

---

# Choosing a Mode

This library ships **two security modes**. Set one per service in `application.yml`:

```yaml
krunish:
  auth:
    mode: legacy    # default — loads CommonAutoConfiguration
    mode: generic   # loads GenericCommonAutoConfiguration
```

| | Legacy Mode | Generic Mode |
|---|---|---|
| **Config class** | `CommonAutoConfiguration` | `GenericCommonAutoConfiguration` |
| **User ID type** | `Long` only | Any (`Long`, `UUID`, custom) |
| **Claims** | `AuthUser(userId, email)` | Custom record implementing `AuthClaims` |
| **Context** | `OrgContext` | `AuthContext` |
| **You must provide** | `JwtValidator` impl | `ClaimsExtractor<C>` bean |
| **Default fallback** | None | `DefaultAuthClaims(Long, email)` |
| **When to use** | Existing services | New services / CRM |

Both configs are **mutually exclusive** — exactly one loads per service. Never both.

---

---

# DTO Package

```text
com.krunish.common.dto
 ├── ApiResponse
 ├── ApiError
 └── ApiPage
```

Provides standard API response structure for all services.

---

## 1. ApiResponse\<T\>

**Purpose:** Wrap all successful API responses.

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

**Usage:**
```java
@GetMapping("/{id}")
public ApiResponse<UserDto> get(@PathVariable UUID id) {
    return ApiResponse.success(userService.get(id));
}
```

---

## 2. ApiError

**Purpose:** Standard error response. Returned automatically by `GlobalExceptionHandler`.

```json
{
  "success": false,
  "message": "USER_NOT_FOUND",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": "2026-02-28T10:00:00"
}
```

Do NOT return manually from controller. Throw `AppException` instead.

---

## 3. ApiPage\<T\>

**Purpose:** Standard pagination wrapper.

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

**Usage:**
```java
@GetMapping
public ApiResponse<ApiPage<UserDto>> getAll(Pageable pageable) {
    Page<User> page = userService.getAll(pageable);
    return ApiResponse.success(ApiPage.from(page));
}
```

---

## Controller Rules

- Always return `ApiResponse<T>`
- Never return Entity directly
- Never return `Page` directly
- Never build error JSON manually

---

---

# Event Package

```text
com.krunish.common.event
 ├── BaseOrgEntity
 ├── KafkaEventEnvelope
 └── OrgIdFilter
```

---

## 1. BaseOrgEntity

**Purpose:** Base class for all organization-scoped business entities.

```java
@Entity
public class Product extends BaseOrgEntity {
}
```

Use in: Product, Order, Invoice, Subscription, any org-owned data.

Do NOT use in: User (global), Organization, UserOrganization (mapping table).

---

## 2. OrgIdFilter

**Purpose:** Hibernate filter for automatic tenant isolation.

Applies automatically:
```sql
WHERE org_id = :orgId
```

No manual filtering needed in repositories.

**Flow:**
```
Request → OrgContext set → Hibernate filter enabled → All queries auto-filtered
```

---

## 3. KafkaEventEnvelope\<T\>

**Purpose:** Standard wrapper for Kafka messages. Ensures events are tenant-aware.

```json
{
  "eventId": "uuid",
  "eventType": "PRODUCT_CREATED",
  "orgId": "uuid",
  "timestamp": "2026-02-28T10:00:00",
  "payload": {}
}
```

**Producer:**
```java
KafkaEventEnvelope<ProductDto> event =
    new KafkaEventEnvelope<>("PRODUCT_CREATED", orgId, productDto);
kafkaTemplate.send("product-topic", event);
```

**Consumer:**
```java
@KafkaListener(topics = "product-topic")
public void consume(KafkaEventEnvelope<ProductDto> event) {
    UUID orgId = event.getOrgId();
    ProductDto payload = event.getPayload();
}
```

---

---

# Exception Package

```text
com.krunish.common.exception
 ├── AppException
 └── GlobalExceptionHandler
```

---

## 1. AppException

**Purpose:** Throw business-level errors from the service layer.

```java
if (product == null) {
    throw new AppException("PRODUCT_NOT_FOUND");
}
```

Use inside: Service classes, validation logic, security checks.
Do NOT use for building controller responses.

---

## 2. GlobalExceptionHandler

**Purpose:** Catches all exceptions globally and converts them to `ApiError` JSON.

Registered via `@RestControllerAdvice`. No manual usage required.

**Flow:**
```
Service throws AppException → GlobalExceptionHandler catches → Returns ApiError JSON
```

---

---

# Security Package — Legacy Mode

> Active when `krunish.auth.mode=legacy` (or property not set).

```text
com.krunish.common.security
 ├── AuthUser
 ├── JwtValidator
 ├── JwtFilter
 ├── OrgAccessValidator
 ├── OrgContext
 ├── HibernateFilterConfigurer
 ├── AuthProperties
 └── AuthSecurityProperties
```

## Security Flow (Legacy)

```
Request
 ↓
JwtFilter
 ↓
JwtValidator  (you implement)
 ↓
OrgAccessValidator  (you implement)
 ↓
OrgContext.set(userId, orgId, email)
 ↓
HibernateFilterConfigurer
 ↓
Controller
```

---

## 1. AuthUser

Represents the authenticated user extracted from JWT.

```java
public record AuthUser(Long userId, String email) {}
```

Returned by `JwtValidator`, stored in `OrgContext`.

---

## 2. JwtValidator

**You must implement this.**

```java
@Component
public class JwtValidatorImpl implements JwtValidator {
    @Override
    public AuthUser validate(String token) {
        // validate signature, parse claims
        return new AuthUser(userId, email);
    }
}
```

---

## 3. OrgAccessValidator

**You must implement this** if your service uses org-level access control.

```java
@Component
public class OrgAccessValidatorImpl implements OrgAccessValidator {
    @Override
    public void validate(UUID userId, UUID orgId) {
        if (!userOrgRepository.existsByUserIdAndOrgId(userId, orgId)) {
            throw new AppException("ORG_ACCESS_DENIED");
        }
    }
}
```

Optional — if no bean is registered, org validation is skipped.

---

## 4. OrgContext

Thread-local store for the current request context. Cleared automatically after request.

```java
Long userId  = OrgContext.getUserId();
Long orgId   = OrgContext.getOrgId();
String email = OrgContext.getEmail();
```

---

## 5. JwtFilter

Runs once per request. Responsibilities:
- Extracts `Bearer` token from `Authorization` header
- Calls `JwtValidator`
- Reads `X-ORG-ID` header (if `OrgAccessValidator` is registered)
- Calls `OrgAccessValidator`
- Sets `OrgContext`

No controller-level security code needed.

---

---

# Security Package — Generic Mode

> Active when `krunish.auth.mode=generic`.

```text
com.krunish.common.security.generic
 ├── AuthClaims                  (marker interface)
 ├── DefaultAuthClaims           (built-in: Long userId + email)
 ├── ClaimsExtractor<C>          (you provide — maps JWT Claims → your record)
 ├── DefaultClaimsExtractor      (built-in fallback for Long services)
 ├── TokenValidator<C>           (interface — validate token → typed claims)
 ├── GenericJwtTokenValidator<C> (built-in JWT impl of TokenValidator)
 ├── AuthFilter                  (marker interface for the filter)
 ├── GenericJwtFilter<C>         (built-in filter — sets AuthContext)
 ├── GenericOrgAccessValidator   (optional — you implement for org checks)
 └── AuthContext                 (thread-local — getClaims(), getOrgId())
```

## Security Flow (Generic)

```
Request
 ↓
GenericJwtFilter
 ↓
TokenValidator.validate(token)         (GenericJwtTokenValidator by default)
 ↓
ClaimsExtractor.extract(rawClaims)     (you provide)
 ↓
GenericOrgAccessValidator.validate()   (optional — you implement)
 ↓
AuthContext.set(claims, orgId)
 ↓
HibernateFilterConfigurer              (if GenericOrgAccessValidator bean exists)
 ↓
Controller
```

---

## 1. AuthClaims

Marker interface. Every service defines its own record implementing it.

```java
// Built-in fallback (Long services)
public record DefaultAuthClaims(Long userId, String email) implements AuthClaims {}

// CRM service example (UUID + rich claims)
public record CrmAuthClaims(
    UUID userId,
    String email,
    String role,
    String globalStatus,
    String kycStatus
) implements AuthClaims {}
```

---

## 2. ClaimsExtractor\<C\>

**The only bean you must provide** in generic mode. Maps raw JJWT `Claims` to your typed record.

```java
// In your @Configuration class
@Bean
public ClaimsExtractor<CrmAuthClaims> claimsExtractor() {
    return claims -> new CrmAuthClaims(
        UUID.fromString(claims.getSubject()),
        claims.get("email", String.class),
        claims.get("role", String.class),
        claims.get("globalStatus", String.class),
        claims.get("kycStatus", String.class)
    );
}
```

If no bean is provided, `DefaultClaimsExtractor` is used (reads `sub` as `Long` + `email`).

---

## 3. TokenValidator\<C\>

Interface for token validation. `GenericJwtTokenValidator` is auto-registered if no custom bean is found.

To replace with a custom strategy (e.g. opaque tokens):
```java
@Bean
public TokenValidator<CrmAuthClaims> tokenValidator() {
    return token -> {
        // custom validation logic
        return new CrmAuthClaims(...);
    };
}
```

---

## 4. AuthFilter

Marker interface implemented by `GenericJwtFilter`. Also extends `jakarta.servlet.Filter` so it can be added to the filter chain safely.

To provide a fully custom filter:
```java
@Component
public class MyFilter extends OncePerRequestFilter implements AuthFilter {
    // GenericJwtFilter will NOT be registered — AuthFilter bean already exists
}
```

---

## 5. GenericJwtFilter\<C\>

Auto-registered. Handles the full auth flow per request:
- Skips public paths
- Extracts `Bearer` token
- Calls `TokenValidator`
- Optionally calls `GenericOrgAccessValidator`
- Sets `AuthContext`
- Clears `AuthContext` in `finally`

---

## 6. GenericOrgAccessValidator

Optional. Implement if your service needs org-level access control.

```java
@Component
public class MyOrgValidator implements GenericOrgAccessValidator {
    @Override
    public void validate(AuthClaims claims, Long orgId) {
        CrmAuthClaims c = (CrmAuthClaims) claims;
        if (!membershipRepo.existsByUserIdAndOrgId(c.userId(), orgId)) {
            throw new AppException("ORG_ACCESS_DENIED");
        }
    }
}
```

If no bean is registered, org validation is skipped entirely.

---

## 7. AuthContext

Thread-local store for the current request context. Replaces `OrgContext` in generic mode. Cleared automatically after request.

```java
// CRM service (UUID claims)
CrmAuthClaims claims = AuthContext.getClaims();
UUID   userId       = claims.userId();
String role         = claims.role();
String kycStatus    = claims.kycStatus();
Long   orgId        = AuthContext.getOrgId();
boolean isService   = AuthContext.isServiceToken();

// Legacy Long service using DefaultAuthClaims
DefaultAuthClaims claims = AuthContext.getClaims();
Long   userId = claims.userId();
String email  = claims.email();
```

---

---

# How To Use In A Service

## Step 1 — Add dependency

```xml
<dependency>
    <groupId>com.krunish</groupId>
    <artifactId>common</artifactId>
</dependency>
```

## Step 2 — Choose mode and configure

```yaml
krunish:
  auth:
    mode: generic        # or legacy
    public-paths:
      - /api/v1/auth/**
      - /actuator/**
    security:
      secret: your-secret-key
```

## Step 3 — Provide required beans

### Legacy mode — implement two interfaces:

```java
@Component
public class JwtValidatorImpl implements JwtValidator { ... }

@Component  // optional — skip if no org filtering needed
public class OrgAccessValidatorImpl implements OrgAccessValidator { ... }
```

### Generic mode — provide one lambda bean:

```java
@Configuration
public class SecurityConfig {

    // UUID-based service
    @Bean
    public ClaimsExtractor<CrmAuthClaims> claimsExtractor() {
        return claims -> new CrmAuthClaims(
            UUID.fromString(claims.getSubject()),
            claims.get("email", String.class),
            claims.get("role", String.class),
            claims.get("globalStatus", String.class),
            claims.get("kycStatus", String.class)
        );
    }

    // Optional — only if org filtering is needed
    @Bean
    public GenericOrgAccessValidator orgAccessValidator(...) {
        return (claims, orgId) -> { ... };
    }
}
```

## Step 4 — Nothing else

`SecurityFilterChain`, `JwtFilter`, `TokenValidator` are all auto-configured. No Spring Security config class needed in the service.

---

---

# Adding a New JWT Field (Generic Mode)

Zero changes to common-lib. Three steps in your service only:

```
1. Add field to your AuthClaims record
      CrmAuthClaims(UUID userId, String email, String role, String globalStatus, String kycStatus, String tenantId)

2. Add claim extraction in SecurityConfig
      claims.get("tenantId", String.class)

3. Encode it in JwtIssuer when minting tokens
```

Done.

---

---

# What Developers Must NOT Do

- Do NOT validate JWT in controller
- Do NOT manually check org membership in controller
- Do NOT manually add `WHERE org_id` in repository queries
- Do NOT parse token strings manually
- Do NOT return Entity directly from controller
- Do NOT build error JSON manually in controller
- Do NOT call `AuthContext.set()` or `OrgContext.set()` manually
- Do NOT mix legacy and generic mode in the same service

---

---

# Responsibility Table

## Legacy Mode

| Class | Responsibility |
|---|---|
| `JwtFilter` | Entry security filter |
| `JwtValidator` | Validate JWT → `AuthUser` |
| `OrgAccessValidator` | Validate user-org membership |
| `OrgContext` | Thread-local request context |
| `HibernateFilterConfigurer` | Apply DB org filter |
| `AuthProperties` | Public paths + security config |
| `AuthUser` | Authenticated user model |

## Generic Mode

| Class | Responsibility |
|---|---|
| `GenericJwtFilter` | Entry security filter |
| `TokenValidator` | Interface — validate token → typed claims |
| `GenericJwtTokenValidator` | Default JWT impl of `TokenValidator` |
| `ClaimsExtractor` | Map raw JWT claims → your typed record |
| `DefaultClaimsExtractor` | Fallback extractor (Long + email) |
| `AuthClaims` | Marker interface for claims records |
| `DefaultAuthClaims` | Built-in fallback claims (Long + email) |
| `AuthFilter` | Marker interface for the security filter |
| `GenericOrgAccessValidator` | Optional — validate user-org membership |
| `AuthContext` | Thread-local request context |
| `HibernateFilterConfigurer` | Apply DB org filter |
| `AuthProperties` | Public paths + security config |

## Shared

| Class | Responsibility |
|---|---|
| `CommonAutoConfiguration` | Auto-wires legacy mode |
| `GenericCommonAutoConfiguration` | Auto-wires generic mode |
| `AppException` | Business error — thrown in service layer |
| `GlobalExceptionHandler` | Converts exceptions → `ApiError` JSON |
| `ApiResponse<T>` | Standard success response wrapper |
| `ApiError` | Standard error response |
| `ApiPage<T>` | Standard pagination wrapper |
| `BaseOrgEntity` | Org-scoped entity base class |
| `KafkaEventEnvelope<T>` | Tenant-aware Kafka message wrapper |