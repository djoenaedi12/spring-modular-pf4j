# Storage A3S Plugin

Plugin PF4J yang menyediakan **S3-compatible storage provider** untuk `storage-plugin`. Menggunakan AWS SDK v2 dan kompatibel dengan layanan S3-compatible manapun (AWS S3, MinIO, Ceph, DigitalOcean Spaces, dll).

## Cara Kerja

Plugin ini mengimplementasi `FileStorageProviderFactory` (PF4J `ExtensionPoint` dari `core-api`). Saat aplikasi start, `storage-plugin` auto-discover factory ini via PF4J dan menjadikan tipe `"A3S"` tersedia untuk digunakan.

```
storage-plugin                         storage-a3s-plugin
┌──────────────────────┐               ┌──────────────────────────┐
│ StorageProviderRegistry │──discovers──▶│ A3sStorageProviderFactory │
│                      │               │   providerType = "A3S"   │
│ GET /types → ["A3S"] │               └──────────────────────────┘
└──────────────────────┘
```

## Konfigurasi

Setelah plugin ter-deploy, register instance A3S via Admin API:

```bash
POST /api/v1/admin/storage-providers
Content-Type: application/json

{
  "code": "a3s-production",
  "name": "A3S Production Bucket",
  "providerType": "A3S",
  "config": {
    "endpoint": "https://s3.ap-southeast-1.amazonaws.com",
    "region": "ap-southeast-1",
    "accessKey": "AKIAIOSFODNN7EXAMPLE",
    "secretKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
    "bucket": "my-app-files"
  },
  "isDefault": false,
  "enabled": true
}
```

### Config Fields

| Key | Type | Required | Deskripsi |
|---|---|---|---|
| `endpoint` | STRING | ✅ | S3-compatible endpoint URL |
| `region` | STRING | ✅ | AWS region (e.g. `ap-southeast-1`) |
| `accessKey` | SECRET | ✅ | Access Key ID |
| `secretKey` | SECRET | ✅ | Secret Access Key |
| `bucket` | STRING | ✅ | Target bucket name |

### Contoh Endpoint untuk Berbagai Layanan

| Layanan | Endpoint |
|---|---|
| AWS S3 | `https://s3.ap-southeast-1.amazonaws.com` |
| MinIO | `http://minio.internal:9000` |
| DigitalOcean Spaces | `https://sgp1.digitaloceanspaces.com` |
| Ceph Object Gateway | `http://ceph-rgw:7480` |

## Path Structure

File disimpan di S3 dengan path: `{resource}/{yyyy/MM/dd}/{fileKey}.{ext}`

Contoh: `USER_AVATAR/2026/05/18/550e8400-e29b-41d4-a716-446655440000.jpg`

## Alur Penggunaan

```
1. Deploy JAR plugin
2. Restart app → PF4J discover A3sStorageProviderFactory
3. GET  /api/v1/admin/storage-providers/types
   → Response: [{ "providerType": "A3S", "configFields": [...] }]
4. POST /api/v1/admin/storage-providers → register instance
5. POST /api/v1/admin/storage-providers/mappings → set routing (opsional)
6. POST /api/v1/medias → upload file → otomatis ke A3S
```

## Dependency

- **AWS SDK v2** (`software.amazon.awssdk:s3`) — S3 client
- `forcePathStyle(true)` — diaktifkan untuk kompatibilitas dengan non-AWS services

## Package Structure

```
gasi.gps.storage.a3s
├── StorageA3SPlugin.java               # PF4J plugin entry point
├── A3sStorageProviderFactory.java      # FileStorageProviderFactory implementation
│   └── A3sFileStorageProvider          # FileStorageProvider inner class
└── extension/
    └── StorageA3SAppExtension.java     # Plugin metadata
```

## Membuat Provider Plugin Baru (Template)

Jika ingin membuat provider plugin lain (misal GCS, Azure), ikuti pola yang sama:

```
plugins/storage-xxx-plugin/
├── pom.xml                             # dependency: core-api + SDK
└── src/main/java/gasi/gps/storage/xxx/
    ├── XxxStoragePlugin.java           # extends Plugin
    ├── XxxStorageProviderFactory.java  # @Extension, implements FileStorageProviderFactory
    └── extension/
        └── StorageXxxAppExtension.java # plugin metadata
```

Yang wajib diimplementasi di factory:

| Method | Fungsi |
|---|---|
| `getProviderType()` | Return identifier unik, misal `"GCS"` |
| `getConfigFields()` | Daftar config yang dibutuhkan |
| `validate(config)` | Validasi config sebelum save ke DB |
| `create(config)` | Buat `FileStorageProvider` instance dari config |
