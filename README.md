# GPS v4 — Spring Modular PF4J

Konsolidasi dua legacy system (**GPS Payroll** + **GPC HRIS**) ke satu unified **Spring Boot** application menggunakan [PF4J](https://pf4j.org/) sebagai plugin framework. Setiap domain fitur dikemas sebagai plugin yang dapat di-deploy secara independen tanpa recompile aplikasi utama.

---

## Arsitektur

```
spring-modular-pf4j/
├── pom.xml              # Parent POM (Maven Multi-Module)
├── core-api/            # Kontrak murni: interfaces, extension points, base DTOs
├── core-app/            # Spring Boot host application
├── auth-plugin/         # Plugin: Autentikasi & Otorisasi
├── audit-plugin/        # Plugin: Audit log
├── ldap-plugin/         # Plugin: Integrasi LDAP / auth provider
└── core-app/plugins/    # Folder runtime untuk JAR plugin
```

### Alur Plugin Loading

```
┌──────────────┐  depends on  ┌──────────────┐
│   core-app   │ ────────────▶│   core-api   │
└──────┬───────┘              └──────▲───────┘
       │ loads JAR from /plugins     │ depends on (provided)
       ▼                             │
┌─────────────────┐    ┌────────────────┐    ┌─────────────────┐
│  auth-plugin    │    │  audit-plugin  │    │  ldap-plugin    │
│  (JAR plugin)   │    │  (JAR plugin)  │    │  (JAR plugin)   │
└─────────────────┘    └────────────────┘    └─────────────────┘
```

1. **`core-api`** — Layer kontrak murni: extension points, base model, base DTO, base repository port, base mapper. Tidak boleh depend ke `core-app` atau modul lain.
2. **`core-app`** — Host application Spring Boot. Saat startup, memuat semua plugin JAR dari `plugins/`, menggabungkan classloader, lalu menjalankan Spring context.
3. **`auth-plugin`** — Plugin autentikasi & otorisasi: JWT, manajemen User, Role, Menu, Permission, RecordRule, AppClient, UserSession, UserDevice, PasswordHistory, PasswordReset.
4. **`audit-plugin`** — Plugin audit log dengan AOP-based interceptor.
5. **`ldap-plugin`** — Plugin integrasi LDAP sebagai auth provider eksternal.

---

## Tech Stack

| Komponen        | Versi           |
|-----------------|-----------------|
| Java            | 25              |
| Spring Boot     | 4.0.3           |
| PF4J Spring     | 0.10.0          |
| Database        | MariaDB 11.8    |
| Migration       | Flyway          |
| ORM Mapper      | MapStruct 1.6.3 |
| Code Generation | Lombok          |
| JWT Library     | JJWT 0.12.6     |
| ID Obfuscation  | Sqids 0.1.0     |
| Cache           | Caffeine        |
| Build Tool      | Maven           |

---

## Getting Started

### Prasyarat

- **Java 25+**
- **Maven 3.8+**
- **MariaDB** berjalan di `localhost:3306`

### Setup Database

Buat database dan user di MariaDB:

```sql
CREATE DATABASE demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gasi'@'localhost' IDENTIFIED BY 'Very$ecret12';
GRANT ALL PRIVILEGES ON demo.* TO 'gasi'@'localhost';
FLUSH PRIVILEGES;
```

### Build

```bash
# Build semua module dari root project
mvn clean install
```

### Copy Plugin ke Folder `plugins/`

Setelah build, salin JAR plugin ke folder `plugins/` di dalam `core-app`:

```bash
mkdir -p core-app/plugins
cp auth-plugin/target/auth-plugin-1.0.0.jar core-app/plugins/
cp audit-plugin/target/audit-plugin-1.0.0.jar core-app/plugins/
cp ldap-plugin/target/ldap-plugin-1.0.0.jar core-app/plugins/
```

### Jalankan Aplikasi

```bash
cd core-app
mvn spring-boot:run
```

---

## Konfigurasi

File konfigurasi utama: [core-app/src/main/resources/application.properties](core-app/src/main/resources/application.properties)

```properties
spring.application.name=core-app
server.port=8080

# MariaDB
spring.datasource.url=jdbc:mariadb://localhost:3306/demo
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.username=gasi
spring.datasource.password=Very$ecret12

# Hibernate: schema dikelola Flyway
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Flyway: dikelola manual via PluginFlywayConfig
spring.flyway.enabled=false

# Bean override dari plugin
spring.main.allow-bean-definition-overriding=true

# ID Encoding (Sqids)
app.id.salt=<your-salt>
```

---

## Flyway Migration Versioning

Semua migration menggunakan format **datetime** sebagai versi:

```
V<YYYYMMDDHHmmss>__<deskripsi>.sql

Contoh:
  V20260306113000__auth_schema_init.sql
  V20260305210727__create_sequences.sql
```

Penomoran tidak dibatasi per-range, cukup pastikan timestamp unik dan monotonically increasing per plugin.

---

## Konvensi & Arsitektur

Setiap plugin mengikuti **Clean Architecture** dengan layer berikut:

```
plugin-xxx/
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
    └── infrastructure/
        ├── adapter/        # Implementasi repository port
        ├── entity/         # JPA Entity
        ├── mapper/         # MapStruct Entity ↔ Model mapper
        ├── persistence/    # Spring Data JPA Repository interfaces
        └── security/       # Security config (khusus auth-plugin)
    └── presentation/
        └── controller/     # REST Controller
```

Detail konvensi ada di folder [docs/conventions/](docs/conventions/):

| File | Isi |
|------|-----|
| [clean-architecture.md](docs/conventions/clean-architecture.md) | Aturan layer dan dependency |
| [database-naming.md](docs/conventions/database-naming.md) | Penamaan tabel & kolom |
| [javadoc.md](docs/conventions/javadoc.md) | Standar JavaDoc |
| [unit-test.md](docs/conventions/unit-test.md) | Standar unit test |

---

## Extension Points (`core-api`)

`core-api` mendefinisikan kontrak yang digunakan oleh semua plugin:

| Interface                   | Deskripsi                                              |
|-----------------------------|--------------------------------------------------------|
| `AppExtension`              | Info dasar plugin (nama, deskripsi)                    |
| `FlywayMigrationExtension`  | Mendaftarkan lokasi migration Flyway dari plugin       |
| `I18nExtension`             | Mendaftarkan lokasi pesan i18n dari plugin             |

---

## Key Design Decisions

| Aspek | Keputusan |
|-------|-----------|
| **Public ID** | Sqids (obfuscation), DB tetap pakai integer PK. Response API **tidak boleh** expose raw DB ID. |
| **Approval Workflow** | Shadow record pattern: `source_id`, `pending_action`, `lifecycle_status`, `approval_status` |
| **Enkripsi** | AES-256-GCM di application layer untuk field sensitif, bukan DB-level encryption |
| **Audit** | `@AuditableEntity` + `@Auditable` + AOP, context via `AuditContext` ThreadLocal |
| **Validasi dinamis** | `@ValidCustom` dengan PF4J extension points |
| **ID Encoding di Mapper** | Gunakan `@Named` qualifier untuk `IdEncoder`, role resolution di service layer bukan mapper |

---

## Membuat Plugin Baru

Gunakan slash command `/create-plugin` di Claude Code untuk panduan lengkap step-by-step.

Workflow tersedia di [.claude/commands/create-plugin.md](.claude/commands/create-plugin.md).

---

## Commands

```bash
mvn clean install       # Build semua module
mvn test                # Unit test
mvn verify              # Integration test
```

---

## Lisensi

Project internal — untuk keperluan pengembangan GPS v4.
