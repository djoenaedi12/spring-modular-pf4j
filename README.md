# Spring Modular PF4J

Unified **Spring Boot** application menggunakan [PF4J](https://pf4j.org/) sebagai plugin framework. Setiap domain fitur dikemas sebagai plugin yang dapat di-deploy secara independen tanpa recompile aplikasi utama.

---

## Struktur Project

```
spring-modular-pf4j/
├── pom.xml                  # Parent POM (Maven Multi-Module)
├── core-api/                # Kontrak murni: interfaces, extension points, base DTOs
├── core-starter/            # Spring Boot starter library untuk semua plugin
├── platform-app/            # Spring Boot host application
│   └── plugins/             # Folder runtime JAR plugin
├── plugins/                 # Source semua plugin
│   ├── auth-plugin/
│   ├── audit-plugin/
│   ├── ldap-plugin/
│   └── payroll-plugin/
└── gasi-cli/                # CLI tooling untuk scaffold plugin baru
```

### Alur Plugin Loading

```
┌──────────────────┐  depends on  ┌──────────────┐  ┌────────────────┐
│   platform-app   │ ────────────▶│   core-api   │  │  core-starter  │
└──────┬───────────┘              └──────▲───────┘  └───────▲────────┘
       │ loads JAR from /plugins         │                   │
       ▼                          depends on (provided scope, via host classloader)
┌──────────────────┐   ┌────────────────┐   ┌──────────────────┐
│  auth-plugin     │   │  audit-plugin  │   │  payroll-plugin  │
│  (JAR plugin)    │   │  (JAR plugin)  │   │  (JAR plugin)    │
└──────────────────┘   └────────────────┘   └──────────────────┘
```

1. **`core-api`** — Layer kontrak murni: extension points, base model, base DTO, base repository port, base mapper. Tidak boleh depend ke modul lain.
2. **`core-starter`** — Spring Boot starter yang menyediakan auto-configuration dan base beans untuk semua plugin.
3. **`platform-app`** — Host application Spring Boot. Saat startup, memuat semua plugin JAR dari `plugins/`, menggabungkan classloader, lalu menjalankan Spring context.
4. **`plugins/`** — Berisi semua plugin domain. Setiap plugin mengikuti Clean Architecture dengan layer `application`, `domain`, `infrastructure`, dan `presentation`.
5. **`gasi-cli`** — CLI tooling untuk scaffold plugin baru secara konsisten.

---

## Tech Stack

| Komponen        | Versi           |
|-----------------|-----------------|
| Java            | 25              |
| Spring Boot     | 4.0.3           |
| PF4J            | 3.15.0          |
| PF4J Spring     | 0.10.0          |
| Database        | MariaDB 11.8    |
| Migration       | Flyway          |
| ORM Mapper      | MapStruct 1.6.3 |
| Code Generation | Lombok          |
| ID Obfuscation  | Sqids 0.1.0     |
| Build Tool      | Maven           |

---

## Getting Started

**Prasyarat:** Java 25+, Maven 3.8+, MariaDB, Node.js

```bash
# Install gasi CLI
cd gasi-cli && npm install && npm link

# Build & deploy plugin
gasi plugin build auth
gasi plugin deploy auth

# Jalankan aplikasi dari root project agar dependency module ikut ter-compile
mvn -pl platform-app -am spring-boot:run
```

Sesuaikan datasource, `app.id.salt`, dan CORS di [platform-app/src/main/resources/application.properties](platform-app/src/main/resources/application.properties) sebelum menjalankan.

Lihat [gasi-cli/README.md](gasi-cli/README.md) untuk opsi lengkap (skip-tests, profile, dll).

---

## Flyway Migration Versioning

Semua migration menggunakan format **datetime** sebagai versi:

```
V<YYYYMMDDHHmmss>__<deskripsi>.sql

Contoh:
  V20260306113000__auth_schema_init.sql
  V20260305210727__create_sequences.sql
```

Pastikan timestamp unik dan monotonically increasing per plugin.

---

## Konvensi Arsitektur

Setiap plugin mengikuti **Clean Architecture** dengan struktur:

```
plugins/xxx-plugin/
└── src/main/java/gasi/gps/<domain>/
    ├── application/
    │   ├── dto/            # Request & Response DTO
    │   ├── mapper/         # MapStruct mapper (application level)
    │   └── service/        # Use case implementation
    ├── domain/
    │   ├── model/          # Domain model (POJO, bukan Entity)
    │   └── port/
    │       ├── inbound/    # Service interfaces
    │       └── outbound/   # Repository port interfaces
    ├── infrastructure/
    │   ├── adapter/        # Implementasi repository port
    │   ├── entity/         # JPA Entity
    │   ├── mapper/         # MapStruct Entity ↔ Model mapper
    │   ├── persistence/    # Spring Data JPA Repository interfaces
    └── presentation/
        └── controller/     # REST Controller
```

---

## Konvensi Global

Aturan yang berlaku di **semua plugin** tanpa pengecualian:

| Aspek | Aturan |
|-------|--------|
| **Public ID** | Sqids (obfuscation), DB tetap pakai integer PK. Response API **tidak boleh** expose raw DB ID. |
| **ID Encoding di Mapper** | Gunakan `@Named` qualifier untuk `IdEncoder`; role resolution di service layer, bukan mapper. |

---

## Membuat Plugin Baru

Gunakan `gasi plugin create` — lihat [gasi-cli/README.md](gasi-cli/README.md) untuk panduan lengkap.
