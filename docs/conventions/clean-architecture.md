# Clean Architecture Conventions

## Layer Structure

```
core-api/                          → Shared contract + base implementations
  ├── domain/
  │   ├── model/                   → BaseModel, filter models (GenericFilter, PageResult, etc.)
  │   └── port/
  │       ├── inbound/             → BaseService interface
  │       └── outbound/            → BaseRepositoryPort interface
  ├── application/
  │   ├── dto/                     → BaseSummaryResponse, BaseDetailResponse
  │   ├── exception/               → EntityNotFoundException, BusinessException
  │   ├── mapper/                  → BaseDtoMapper, @IgnoreAuditFields
  │   └── service/                 → BaseServiceImpl (abstract)
  ├── audit/                       → @AuditableEntity, @Auditable, AuditLogExtension, AuditLogSpi
  ├── infrastructure/
  │   ├── adapter/                 → BaseRepositoryAdapter (abstract)
  │   ├── entity/                  → BaseEntity
  │   ├── i18n/                    → MessageUtil, I18nExtension
  │   ├── mapper/                  → BaseMapper, @IgnoreAuditFields, StringArrayMapper
  │   ├── security/                → SecurityContextUtil, AuthProviderExtension, AuthenticatedPrincipal
  │   ├── specification/           → GenericSpecification
  │   └── util/                    → IdEncoder, HashUtil
  └── presentation/
      ├── controller/              → BaseController (abstract)
      └── dto/                     → SearchRequest, ApiResponse

core-app/                          → Bootstrap + cross-cutting infrastructure
  ├── CoreApplication.java         → Spring Boot entry point
  ├── infrastructure/
  │   ├── classloader/             → CompositeClassLoader (PF4J support)
  │   └── config/                  → PluginConfig, CacheConfig, JpaAuditingConfig,
  │                                   PluginFlywayConfig, PluginMessageSourceConfig
  └── presentation/
      ├── controller/              → CoreController
      └── handler/                 → GlobalExceptionHandler

plugin modules/                    → Feature modules via PF4J
  ├── domain/
  │   ├── model/                   → Domain models (extends BaseModel)
  │   └── port/
  │       ├── inbound/             → Service interfaces (extends BaseService)
  │       └── outbound/            → Repository ports (extends BaseRepositoryPort)
  ├── application/
  │   ├── dto/                     → CreateRequest, UpdateRequest, SummaryResponse, DetailResponse
  │   ├── mapper/                  → DtoMapper (extends BaseDtoMapper)
  │   └── service/                 → ServiceImpl (extends BaseServiceImpl)
  ├── infrastructure/
  │   ├── adapter/                 → RepositoryAdapter (extends BaseRepositoryAdapter)
  │   ├── entity/                  → JPA entities (extends BaseEntity)
  │   ├── mapper/                  → Entity mappers (extends BaseMapper)
  │   ├── persistence/             → Spring Data JPA repositories
  │   └── validation/              → Custom validators (annotation + ConstraintValidator)
  └── presentation/
      └── controller/              → REST controllers (extends BaseController)
```

## Dependency Rules

```
ALLOWED:
  core-app     → core-api        ✅
  plugin       → core-api        ✅
  plugin       → plugin (own)    ✅

FORBIDDEN:
  core-api     → core-app        ❌
  plugin       → core-app        ❌ (gunakan interface dari core-api)
  plugin       → plugin (other)  ❌
```

## Naming Conventions per Layer

### Inbound Port (Interface) — `domain/port/inbound/`
- `{E}Service` → e.g. `AppClientService`, `UserService`
- Extends `BaseService<M, CReq, UReq, SRes, DRes>`
- JANGAN tambahkan `Impl` di nama interface

### Outbound Port (Interface) — `domain/port/outbound/`
- `{E}RepositoryPort` → e.g. `AppClientRepositoryPort`
- Extends `BaseRepositoryPort<M>`

### Service Implementation — `application/service/`
- `{E}ServiceImpl implements {E}Service`
- Extends `BaseServiceImpl<M, CReq, UReq, SRes, DRes>`
- Override `resourceType()` wajib

### Repository Adapter — `infrastructure/adapter/`
- `{E}RepositoryAdapter implements {E}RepositoryPort`
- Extends `BaseRepositoryAdapter<M, E>`
- Override `resourceType()` wajib

### DTO — `application/dto/`
- Request: `{E}CreateRequest`, `{E}UpdateRequest`
- Response: `{E}SummaryResponse extends BaseSummaryResponse`, `{E}DetailResponse extends BaseDetailResponse`
- JANGAN gunakan entity/domain model sebagai API request/response

### Mappers — dua jenis, dua lokasi
- DTO Mapper: `{E}DtoMapper extends BaseDtoMapper` — di `application/mapper/`
  - `@Mapper(componentModel = "spring", uses = { IdEncoder.class })`
- Entity Mapper: `{E}Mapper extends BaseMapper` — di `infrastructure/mapper/`
  - `@Mapper`

### Controller — `presentation/controller/`
- `{E}Controller extends BaseController`
- `@RestController @RequestMapping`
- Hanya memanggil service interface, BUKAN service impl langsung

### JPA Repository — `infrastructure/persistence/`
- `{E}EntityRepository extends JpaRepository<{E}Entity, Long>, JpaSpecificationExecutor<{E}Entity>`
- `@Repository`

### Domain Model — `domain/model/`
- Extends `BaseModel`
- `@Data @SuperBuilder(toBuilder=true) @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=true)`
- Tidak ada JPA annotations

### JPA Entity — `infrastructure/entity/`
- `{E}Entity extends BaseEntity`
- `@Entity @Table(name = "...")`
- Mirror dari domain model dengan JPA annotations

## Rules

1. Controller → hanya panggil `{E}Service` interface, bukan `{E}ServiceImpl` langsung
2. ServiceImpl → hanya panggil `{E}RepositoryPort`, TIDAK boleh akses `{E}EntityRepository` langsung
3. Domain model TIDAK BOLEH keluar dari service layer — selalu convert ke DTO via mapper
4. Business logic HANYA di service layer, BUKAN di controller atau repository adapter
5. Cross-cutting concerns (audit, logging, security) via AOP/annotation
6. Plugin berkomunikasi dengan core hanya melalui interface/class di `core-api`
7. ID yang keluar ke response WAJIB di-encode via `IdEncoder` (Sqids), jangan expose raw DB id
8. Role resolution dilakukan di service layer, BUKAN di mapper
