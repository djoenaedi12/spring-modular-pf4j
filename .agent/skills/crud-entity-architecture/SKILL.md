---
name: crud-entity-architecture
description: Architecture pattern and file structure for creating CRUD entities following the project's clean architecture with base classes
---

# CRUD Entity Architecture

Panduan lengkap file-by-file yang harus dibuat untuk setiap entity CRUD baru. Setiap entity membutuhkan **14 file** yang mengikuti pattern clean architecture yang sudah ada.

## Overview: 14 Files per Entity

Ganti `<E>` dengan nama entity (PascalCase), `<e>` (lowercase), `<plugin>` dengan nama plugin.

```
<plugin>/src/main/java/gasi/gps/<plugin>/
├── domain/
│   ├── model/<E>.java                              # 1. Domain model
│   └── port/
│       ├── inbound/<E>Service.java                  # 2. Service interface
│       └── outbound/<E>RepositoryPort.java          # 3. Repository port
├── application/
│   ├── dto/
│   │   ├── <E>CreateRequest.java                    # 4. Create DTO
│   │   ├── <E>UpdateRequest.java                    # 5. Update DTO
│   │   ├── <E>SummaryResponse.java                  # 6. Summary response
│   │   └── <E>DetailResponse.java                   # 7. Detail response
│   ├── mapper/<E>DtoMapper.java                     # 8. DTO mapper
│   └── service/<E>ServiceImpl.java                  # 9. Service impl
├── infrastructure/
│   ├── entity/<E>Entity.java                        # 10. JPA entity
│   ├── mapper/<E>Mapper.java                        # 11. Entity mapper
│   ├── adapter/<E>RepositoryAdapter.java            # 12. Repository adapter
│   └── persistence/<E>EntityRepository.java         # 13. JPA repository
└── presentation/
    └── controller/<E>Controller.java                # 14. REST controller
```

Plus **1 SQL migration** file di `src/main/resources/db/migration/`.

---

## Layer 1: Domain

### 1. Domain Model — `domain/model/<E>.java`

Extends `BaseModel`. Hanya business fields, tidak ada JPA annotations.

```java
package gasi.gps.<plugin>.domain.model;

import gasi.gps.core.api.domain.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Domain model representing a <deskripsi>.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class <E> extends BaseModel {

    private String code;
    private String name;
    // ... business fields
}
```

### 2. Service Interface — `domain/port/inbound/<E>Service.java`

Extends `BaseService`. Tambahkan custom methods jika perlu.

```java
package gasi.gps.<plugin>.domain.port.inbound;

import gasi.gps.<plugin>.application.dto.*;
import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.core.api.domain.port.inbound.BaseService;

/**
 * Inbound port for <E> CRUD operations.
 */
public interface <E>Service extends
        BaseService<<E>, <E>CreateRequest, <E>UpdateRequest, <E>SummaryResponse, <E>DetailResponse> {
}
```

### 3. Repository Port — `domain/port/outbound/<E>RepositoryPort.java`

Extends `BaseRepositoryPort`. Tambahkan custom query methods jika perlu.

```java
package gasi.gps.<plugin>.domain.port.outbound;

import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort;

/**
 * Outbound port for <E> persistence.
 */
public interface <E>RepositoryPort extends BaseRepositoryPort<<E>> {
}
```

---

## Layer 2: Application

### 4. Create Request — `application/dto/<E>CreateRequest.java`

Plain DTO dengan Jakarta Validation.

```java
package gasi.gps.<plugin>.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new <E>.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class <E>CreateRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;
}
```

### 5. Update Request — `application/dto/<E>UpdateRequest.java`

Biasanya sama dengan `CreateRequest` tapi bisa berbeda.

```java
package gasi.gps.<plugin>.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a <E>.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class <E>UpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;
}
```

### 6. Summary Response — `application/dto/<E>SummaryResponse.java`

Extends `BaseSummaryResponse` (includes `id`). Untuk list/search results.

```java
package gasi.gps.<plugin>.application.dto;

import gasi.gps.core.api.application.dto.BaseSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Summary response DTO for <E> listing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class <E>SummaryResponse extends BaseSummaryResponse {

    private String code;
    private String name;
}
```

### 7. Detail Response — `application/dto/<E>DetailResponse.java`

Extends `BaseDetailResponse` (includes `id` + audit fields). Untuk single entity.

```java
package gasi.gps.<plugin>.application.dto;

import gasi.gps.core.api.application.dto.BaseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Detail response DTO for a single <E>.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class <E>DetailResponse extends BaseDetailResponse {

    private String code;
    private String name;
    private String description;
}
```

### 8. DTO Mapper — `application/mapper/<E>DtoMapper.java`

MapStruct mapper extending `BaseDtoMapper`. Pakai `IdEncoder` untuk encode ID.

```java
package gasi.gps.<plugin>.application.mapper;

import org.mapstruct.Mapper;

import gasi.gps.<plugin>.application.dto.*;
import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.core.api.application.mapper.BaseDtoMapper;
import gasi.gps.core.api.infrastructure.util.IdEncoder;

@Mapper(componentModel = "spring", uses = { IdEncoder.class })
public interface <E>DtoMapper extends
        BaseDtoMapper<<E>, <E>CreateRequest, <E>UpdateRequest, <E>SummaryResponse, <E>DetailResponse> {
}
```

> **Note:** `BaseDtoMapper` sudah define 5 methods: `toCreateDomain`, `toUpdateDomain`, `toSummary`, `toDetail`, `updateDomain`. Tidak perlu implement manual kecuali ada custom mapping.

### 9. Service Implementation — `application/service/<E>ServiceImpl.java`

Extends `BaseServiceImpl`. Override `resourceType()`. Tambahkan custom logic jika perlu.

```java
package gasi.gps.<plugin>.application.service;

import org.springframework.stereotype.Service;

import gasi.gps.<plugin>.application.dto.*;
import gasi.gps.<plugin>.application.mapper.<E>DtoMapper;
import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.<plugin>.domain.port.inbound.<E>Service;
import gasi.gps.<plugin>.domain.port.outbound.<E>RepositoryPort;
import gasi.gps.core.api.application.service.BaseServiceImpl;
import gasi.gps.core.api.infrastructure.i18n.MessageUtil;

@Service
public class <E>ServiceImpl extends
        BaseServiceImpl<<E>, <E>CreateRequest, <E>UpdateRequest, <E>SummaryResponse, <E>DetailResponse>
        implements <E>Service {

    public <E>ServiceImpl(<E>RepositoryPort repositoryPort,
            <E>DtoMapper mapper,
            MessageUtil messageUtil) {
        super(repositoryPort, mapper, messageUtil);
    }

    @Override
    protected String resourceType() {
        return "<E>";
    }

    // Override validateCreate/validateUpdate/validateDelete jika perlu custom validation
}
```

---

## Layer 3: Infrastructure

### 10. JPA Entity — `infrastructure/entity/<E>Entity.java`

Extends `BaseEntity`. Mirror dari domain model dengan JPA annotations.

```java
package gasi.gps.<plugin>.infrastructure.entity;

import gasi.gps.core.api.infrastructure.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "<table_name>")
public class <E>Entity extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}
```

### 11. Entity Mapper — `infrastructure/mapper/<E>Mapper.java`

MapStruct mapper extending `BaseMapper`. Biasanya empty body.

```java
package gasi.gps.<plugin>.infrastructure.mapper;

import org.mapstruct.Mapper;

import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.<plugin>.infrastructure.entity.<E>Entity;
import gasi.gps.core.api.infrastructure.mapper.BaseMapper;

@Mapper
public interface <E>Mapper extends BaseMapper<<E>, <E>Entity> {
}
```

### 12. Repository Adapter — `infrastructure/adapter/<E>RepositoryAdapter.java`

Extends `BaseRepositoryAdapter`, implements `<E>RepositoryPort`.

```java
package gasi.gps.<plugin>.infrastructure.adapter;

import org.springframework.stereotype.Component;

import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.<plugin>.domain.port.outbound.<E>RepositoryPort;
import gasi.gps.<plugin>.infrastructure.entity.<E>Entity;
import gasi.gps.<plugin>.infrastructure.mapper.<E>Mapper;
import gasi.gps.<plugin>.infrastructure.persistence.<E>EntityRepository;
import gasi.gps.core.api.infrastructure.adapter.BaseRepositoryAdapter;

@Component
public class <E>RepositoryAdapter
        extends BaseRepositoryAdapter<<E>, <E>Entity>
        implements <E>RepositoryPort {

    public <E>RepositoryAdapter(<E>EntityRepository repository,
            <E>Mapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String resourceType() {
        return "<E>";
    }
}
```

### 13. JPA Repository — `infrastructure/persistence/<E>EntityRepository.java`

Extends `JpaRepository` + `JpaSpecificationExecutor` (required by `BaseRepositoryAdapter`).

```java
package gasi.gps.<plugin>.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import gasi.gps.<plugin>.infrastructure.entity.<E>Entity;

/**
 * Spring Data JPA repository for <E>Entity.
 */
@Repository
public interface <E>EntityRepository extends
        JpaRepository<<E>Entity, Long>,
        JpaSpecificationExecutor<<E>Entity> {
}
```

---

## Layer 4: Presentation

### 14. REST Controller — `presentation/controller/<E>Controller.java`

Extends `BaseController`. Endpoints otomatis: CRUD + search + pagination.

```java
package gasi.gps.<plugin>.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.<plugin>.application.dto.*;
import gasi.gps.<plugin>.domain.model.<E>;
import gasi.gps.<plugin>.domain.port.inbound.<E>Service;
import gasi.gps.core.api.infrastructure.util.IdEncoder;
import gasi.gps.core.api.presentation.controller.BaseController;

@RestController
@RequestMapping("/api/<endpoint>")
public class <E>Controller extends
        BaseController<<E>, <E>CreateRequest, <E>UpdateRequest, <E>SummaryResponse, <E>DetailResponse> {

    public <E>Controller(<E>Service service, IdEncoder idEncoder) {
        super(service, idEncoder);
    }
}
```

**Auto-generated endpoints dari `BaseController`:**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/<endpoint>` | Create |
| GET | `/api/<endpoint>/{id}` | Get by ID |
| PUT | `/api/<endpoint>/{id}` | Update |
| DELETE | `/api/<endpoint>/{id}` | Delete |
| POST | `/api/<endpoint>/search` | Find one by filter |
| POST | `/api/<endpoint>/search/list` | Find all by filter |
| POST | `/api/<endpoint>/search/page` | Find all paged |

---

## Database Migration

Buat SQL file di `src/main/resources/db/migration/V<N>__create_<table>.sql`:

```sql
CREATE TABLE <table_name> (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    origin_id   BIGINT,
    status      VARCHAR(50),
    version     BIGINT       DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);
```

> Kolom `id`, `origin_id`, `status`, `version`, `created_at`, `updated_at`, `created_by`, `updated_by` wajib ada karena dari `BaseEntity`.

---

## Checklist

Untuk setiap entity baru, pastikan semua file dibuat:

- [ ] Domain Model (`extends BaseModel`)
- [ ] Service Interface (`extends BaseService`)
- [ ] Repository Port (`extends BaseRepositoryPort`)
- [ ] Create Request DTO (dengan validation)
- [ ] Update Request DTO (dengan validation)
- [ ] Summary Response (`extends BaseSummaryResponse`)
- [ ] Detail Response (`extends BaseDetailResponse`)
- [ ] DTO Mapper (`extends BaseDtoMapper`, uses `IdEncoder`)
- [ ] Service Impl (`extends BaseServiceImpl`, override `resourceType()`)
- [ ] JPA Entity (`extends BaseEntity`)
- [ ] Entity Mapper (`extends BaseMapper`)
- [ ] Repository Adapter (`extends BaseRepositoryAdapter`, override `resourceType()`)
- [ ] JPA Repository (`extends JpaRepository + JpaSpecificationExecutor`)
- [ ] Controller (`extends BaseController`)
- [ ] Flyway SQL migration

## Annotations Cheat Sheet

| Class Type | Lombok | Spring/JPA |
|------------|--------|------------|
| Domain Model | `@Data @SuperBuilder(toBuilder=true) @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=true)` | — |
| Request DTO | `@Data @Builder @NoArgsConstructor @AllArgsConstructor` | `@NotBlank @Size` |
| Response DTO | `@Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=true)` | — |
| JPA Entity | `@Data @SuperBuilder(toBuilder=true) @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=true)` | `@Entity @Table` |
| DTO Mapper | — | `@Mapper(componentModel="spring", uses={IdEncoder.class})` |
| Entity Mapper | — | `@Mapper` |
| Service Impl | — | `@Service` |
| Repository Adapter | — | `@Component` |
| JPA Repository | — | `@Repository` |
| Controller | — | `@RestController @RequestMapping` |
