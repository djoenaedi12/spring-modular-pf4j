# Storage Plugin

Plugin inti untuk manajemen file dan konfigurasi storage provider. Menyediakan upload, download, delete file serta admin API untuk mengatur storage provider dan routing.

## Arsitektur

```
storage-plugin (orchestrator)
├── Manages storage providers (CRUD via DB)
├── Manages resource → provider mappings
├── Handles file upload / download / delete
└── Discovers provider factories via PF4J ExtensionPoint

storage-a3s-plugin (provider)          ← plugin terpisah
└── Implements FileStorageProviderFactory

storage-xxx-plugin (provider)          ← plugin baru di masa depan
└── Implements FileStorageProviderFactory
```

## Database Schema

Plugin ini memiliki 3 tabel yang di-manage via Flyway:

| Tabel | Fungsi |
|---|---|
| `storage_providers` | Daftar provider yang terdaftar (code, type, config JSONB) |
| `storage_provider_mappings` | Mapping resource → provider untuk routing |
| `medias` | Metadata file yang diupload (file_key, path, owner) |

## REST API

### Media (Upload / Download / Delete)

| Method | Endpoint | Deskripsi |
|---|---|---|
| `POST` | `/api/v1/medias` | Upload file (multipart) |
| `GET` | `/api/v1/medias/{fileKey}/download` | Download file |
| `DELETE` | `/api/v1/medias/{fileKey}` | Delete file |
| `GET` | `/api/v1/medias?resource=X&resourceId=Y` | List file by owner |

#### Upload

```bash
curl -X POST http://localhost:8080/api/v1/medias \
  -F "file=@photo.jpg" \
  -F "resource=USER_AVATAR" \
  -F "resourceId=abc123"
```

#### Response

```json
{
  "status": 200,
  "data": {
    "id": "encoded-id",
    "fileKey": "550e8400-e29b-41d4-a716-446655440000",
    "originalName": "photo.jpg",
    "contentType": "image/jpeg",
    "fileSize": 102400,
    "checksum": "a1b2c3...",
    "resource": "USER_AVATAR",
    "resourceId": "abc123"
  }
}
```

### Admin — Storage Providers

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/admin/storage-providers/types` | List available types (dari plugin ter-install) |
| `GET` | `/api/v1/admin/storage-providers` | List semua provider |
| `POST` | `/api/v1/admin/storage-providers` | Register provider baru |
| `PUT` | `/api/v1/admin/storage-providers/{id}` | Update provider |
| `DELETE` | `/api/v1/admin/storage-providers/{id}` | Delete provider |
| `PUT` | `/api/v1/admin/storage-providers/{id}/default` | Set sebagai default |

#### Register Provider

```bash
curl -X POST http://localhost:8080/api/v1/admin/storage-providers \
  -H "Content-Type: application/json" \
  -d '{
    "code": "local-dev",
    "name": "Local Development",
    "providerType": "LOCAL",
    "config": {
      "basePath": "./uploads"
    },
    "isDefault": true,
    "enabled": true
  }'
```

### Admin — Mappings

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/v1/admin/storage-providers/mappings` | List semua mapping |
| `POST` | `/api/v1/admin/storage-providers/mappings` | Buat mapping baru |
| `PUT` | `/api/v1/admin/storage-providers/mappings/{id}` | Update mapping |
| `DELETE` | `/api/v1/admin/storage-providers/mappings/{id}` | Delete mapping |

#### Set Resource Mapping

```bash
curl -X POST http://localhost:8080/api/v1/admin/storage-providers/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "resource": "USER_AVATAR",
    "providerId": "encoded-provider-id"
  }'
```

## Routing Logic

```
Upload file dengan resource = "USER_AVATAR"
  │
  ├─ Ada mapping untuk "USER_AVATAR"?
  │   ├─ Ya  → pakai provider dari mapping
  │   └─ Tidak → pakai provider default (is_default = true)
  │
  └─ Provider instance di-cache di StorageProviderRegistry
```

## Built-in Provider

Plugin ini menyertakan **LOCAL** provider secara default:

| Config Key | Type | Required | Default |
|---|---|---|---|
| `basePath` | STRING | ✅ | `./uploads` |

File disimpan dengan struktur: `{basePath}/{resource}/{yyyy/MM/dd}/{fileKey}.{ext}`

## Menambah Provider Baru

Untuk menambah tipe provider baru (misal MinIO, GCS, Azure), buat plugin PF4J baru yang mengimplementasi `FileStorageProviderFactory` dari `core-api`. Lihat `storage-a3s-plugin` sebagai contoh.

Setelah plugin baru ter-deploy:
1. Restart aplikasi → PF4J auto-discover factory
2. `GET /types` → tipe baru muncul
3. `POST /storage-providers` → register instance via API
4. Siap dipakai — tanpa deploy ulang lagi

## Package Structure

```
gasi.gps.storage
├── StoragePlugin.java                            # PF4J plugin entry point
├── extension/
│   ├── StorageAppExtension.java                  # Plugin metadata
│   ├── StorageFlywayMigrationExtension.java      # Flyway migration
│   └── StorageI18nExtension.java                 # i18n messages
├── domain/
│   ├── model/
│   │   ├── Media.java                            # Media domain model
│   │   ├── StorageProvider.java                   # Provider domain model
│   │   └── StorageProviderMapping.java            # Mapping domain model
│   └── port/
│       ├── inbound/MediaService.java              # Service interface
│       └── outbound/MediaRepositoryPort.java      # Repository port
├── application/
│   ├── dto/                                       # Request/Response DTOs
│   └── service/
│       ├── MediaServiceImpl.java                  # Upload/download logic
│       └── StorageProviderAdminService.java       # Admin CRUD
├── infrastructure/
│   ├── entity/                                    # JPA entities
│   ├── mapper/                                    # MapStruct mappers
│   ├── persistence/                               # JPA repositories
│   ├── adapter/MediaRepositoryAdapter.java        # Repository adapter
│   └── storage/
│       ├── StorageProviderRegistry.java           # Cache & factory discovery
│       ├── StorageProviderResolver.java           # DB-based routing
│       └── provider/
│           └── LocalStorageProviderFactory.java   # Built-in LOCAL provider
└── presentation/
    └── controller/
        ├── MediaController.java                   # Media REST API
        └── StorageProviderAdminController.java    # Admin REST API
```
