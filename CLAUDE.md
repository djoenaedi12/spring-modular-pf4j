# GPS v4

Konsolidasi dua legacy system (GPS Payroll + GPC HRIS) ke unified Spring Boot application.

## Tech Stack

Java 25, Spring Boot 4.0.3, Maven multi-module, MariaDB 11.8, PF4J Spring 0.10.0, Clean Architecture (port/adapter), MapStruct 1.6.3, Lombok, Flyway, Caffeine cache, Sqids 0.1.0, JJWT 0.12.6.

## Modules

- `core-api` — pure contract layer (interfaces, annotations, DTOs). TIDAK BOLEH depend ke `core` atau module lain.
- `core-app` — Spring Boot host application, memuat plugin dari `plugins/` folder
- Plugin modules: `auth-plugin`, `audit-plugin`, `ldap-plugin` — masing-masing punya Flyway migration sendiri

## Commands

- `mvn clean install` — build semua module
- `mvn test` — unit test
- `mvn verify` — integration test

## Key Architecture Decisions

- Public ID: Sqids (obfuscation only), DB validation sebagai real gatekeeper. JANGAN expose real DB ID ke response.
- Shadow record pattern untuk approval workflow (`source_id`, `lifecycle_status`)
- Sensitive field encryption: application-level AES-256-GCM, bukan DB-level
- Audit: `@AuditableEntity` + `@Auditable` + AOP, context via `AuditContext` ThreadLocal
- Dynamic validation: `@ValidCustom` dengan PF4J extension points
- MapStruct: `BaseDtoMapper`, `@IgnoreAuditFields`/`@IgnoreSecurityFields`, `@Named` qualifier untuk `IdEncoder`
- Role resolution di service layer, BUKAN di mapper
- Bean decoupling via interface (e.g. `CurrentUserProvider`)
- Actuator endpoint di `/manage`, bypass JWT filter via `shouldNotFilter()`

## Rules & Conventions

Instruksi detail ada di file terpisah. WAJIB baca file yang relevan sebelum mulai coding:

- **Clean Architecture** → lihat `@docs/conventions/clean-architecture.md`
- **Database & Column Naming** → lihat `@docs/conventions/database-naming.md`
- **JavaDoc** → lihat `@docs/conventions/javadoc.md`
- **Unit Test** → lihat `@docs/conventions/unit-test.md`

## Important

- `core-api` HARUS tetap pure contract — tanpa dependency ke `core`
- Semua entity yang masuk ke API response WAJIB encode ID dengan Sqids
- JANGAN buat god class — pisahkan tanggung jawab sesuai Clean Architecture layer
- Saat compacting, SELALU preserve daftar file yang dimodifikasi dan keputusan arsitektur
