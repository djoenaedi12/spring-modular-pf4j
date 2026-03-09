# auth-plugin

Plugin autentikasi dan otorisasi untuk GPS v4. Menyediakan JWT-based authentication, manajemen User/Role/Permission, RBAC dengan Caffeine cache, forgot/reset password, serta manajemen AppClient untuk multi-client integration.

---

## Fitur

- JWT stateless authentication (BCrypt password hashing)
- Login dengan Basic Auth (client credentials) atau username/password
- Forgot & reset password flow
- RBAC: Role → Permission (`RESOURCE:ACTION`) dengan Caffeine cache
- Multi-client: AppClient dengan scopes dan expiry konfigurasi per client
- Manajemen: User, Role, Menu, Action, Resource, RecordRule
- Session tracking: UserSession, UserDevice
- Password history

---

## REST API

### Authentication (public)

| Method | Endpoint                     | Deskripsi |
|--------|------------------------------|-----------|
| POST   | `/api/v1/auth/login`         | Login user, return JWT. Mendukung Basic Auth header untuk client credentials |
| POST   | `/api/v1/auth/forgot-password` | Inisiasi forgot password flow |
| POST   | `/api/v1/auth/reset-password`  | Reset password menggunakan token |

**Login — user credentials:**
```json
POST /api/v1/auth/login
{
  "username": "john.doe",
  "password": "secret"
}
```

**Login — dengan client credentials (Basic Auth):**
```
Authorization: Basic base64(clientId:clientSecret)

POST /api/v1/auth/login
{
  "username": "john.doe",
  "password": "secret",
  "grantType": "password"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer"
  }
}
```

---

### Management (requires JWT)

Semua endpoint management mengikuti pola `BaseController` (CRUD standar):

| Resource       | Base URL                  | Keterangan |
|---------------|---------------------------|------------|
| User          | `/api/v1/users`           | Manajemen user |
| Role          | `/api/v1/roles`           | Manajemen role |
| Menu          | `/api/v1/menus`           | Manajemen menu navigasi |
| Action        | `/api/v1/actions`         | Definisi action (READ, CREATE, dll.) — **public** |
| Resource      | `/api/v1/resources`       | Definisi resource (entitas yang dilindungi) |
| RecordRule    | `/api/v1/record-rules`    | Aturan filter data per role |
| AppClient     | `/api/v1/app-clients`     | Manajemen client aplikasi |

Setiap endpoint management memiliki operasi standar:

| Method | Path         | Operasi |
|--------|--------------|---------|
| GET    | `/`          | List (summary) |
| GET    | `/{id}`      | Detail |
| POST   | `/`          | Create |
| PUT    | `/{id}`      | Update |
| DELETE | `/{id}`      | Delete |

> ID pada path menggunakan **Sqids** (encoded), bukan raw DB ID.

---

## Security

### Endpoint Whitelist (tanpa JWT)

```
/api/v1/auth/**    — login, forgot/reset password
/api/v1/actions/** — daftar actions (untuk keperluan UI)
/manage/**         — actuator endpoints
/error             — error handler
```

Semua endpoint lain wajib JWT valid.

### RBAC — Permission Format

Permission dikodekan sebagai `RESOURCE:ACTION`, contoh:

```
USER:READ
USER:CREATE
ROLE:DELETE
EMPLOYEE:APPROVE
```

`RbacPermissionEvaluator` memvalidasi permission via `@PreAuthorize` Spring Security dengan lookup ke `PermissionCacheService` (Caffeine cache, key: `roleCode`).

### Caffeine Cache

| Cache Key         | Isi |
|------------------|-----|
| `rolePermissions` | `Set<String>` permission untuk setiap `roleCode` |

Cache di-load lazy saat pertama kali role diakses dan di-invalidate saat permission berubah.

---

## Domain Model

| Model           | Keterangan |
|----------------|------------|
| `User`          | Akun pengguna, relasi ke Role |
| `Role`          | Grup permission, punya kode unik (misal `SYS_ADMIN`) |
| `Permission`    | Relasi Role ↔ Resource ↔ Action (`RESOURCE:ACTION`) |
| `Resource`      | Entitas yang dilindungi (misal `Employee`, `Payroll`) |
| `Action`        | Tipe operasi: `READ`, `CREATE`, `UPDATE`, `DELETE`, `APPROVE`, dll. |
| `Menu`          | Item navigasi UI, diikat ke Role via `RoleMenu` |
| `RecordRule`    | Filter data per Role (row-level security) |
| `RoleRecordRule`| Relasi Role ↔ RecordRule |
| `AppClient`     | Client aplikasi untuk multi-client auth (scopes, expiry) |
| `UserSession`   | Tracking sesi aktif per user/device |
| `UserDevice`    | Tracking device yang digunakan user |
| `PasswordHistory`| Riwayat password untuk mencegah reuse |
| `PasswordReset` | Token reset password (one-time, time-limited) |

---

## Struktur Package

```
auth-plugin/src/main/java/gasi/gps/auth/
├── AuthPlugin.java
├── AuthAppExtension.java
├── AuthFlywayExtension.java
├── application/
│   ├── dto/                        # Request & Response DTO
│   │   ├── LoginRequest/Response
│   │   ├── ForgotPasswordRequest
│   │   ├── ResetPasswordRequest
│   │   ├── User*, Role*, Menu*, Action*
│   │   ├── Resource*, RecordRule*, AppClient*
│   │   └── Permission*, RoleRecordRule*
│   ├── mapper/                     # DTO ↔ Domain (MapStruct)
│   └── service/
│       ├── AuthServiceImpl.java    # Login, forgot/reset password
│       ├── UserServiceImpl.java
│       ├── RoleServiceImpl.java
│       ├── MenuServiceImpl.java
│       ├── ActionServiceImpl.java
│       ├── ResourceServiceImpl.java
│       ├── RecordRuleServiceImpl.java
│       ├── AppClientServiceImpl.java
│       └── PermissionCacheService.java  # Caffeine cache untuk RBAC
├── domain/
│   ├── model/                      # Domain model (POJO)
│   └── port/
│       ├── inbound/                # Service interfaces
│       └── outbound/               # Repository port interfaces
├── infrastructure/
│   ├── adapter/                    # Implementasi repository port
│   ├── entity/                     # JPA entities
│   ├── mapper/                     # Entity ↔ Domain (MapStruct)
│   ├── persistence/                # Spring Data JPA repositories
│   ├── security/
│   │   ├── SecurityConfig.java         # Filter chain, CSRF off, stateless
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtUtil.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── RbacPermissionEvaluator.java # hasPermission() untuk @PreAuthorize
│   │   ├── MethodSecurityConfig.java
│   │   ├── SecurityContextUtilImpl.java
│   │   └── provider/
│   │       └── LocalAuthProvider.java
│   └── validation/
│       ├── PasswordMatch.java           # Custom constraint annotation
│       └── PasswordMatchValidator.java
└── presentation/
    └── controller/
        ├── AuthController.java
        ├── UserController.java
        ├── RoleController.java
        ├── MenuController.java
        ├── ActionController.java
        ├── ResourceController.java
        ├── RecordRuleController.java
        └── AppClientController.java
```
