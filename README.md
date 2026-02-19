# Spring Modular PF4J

Aplikasi **Spring Boot modular** yang menggunakan [PF4J](https://pf4j.org/) sebagai plugin framework. Arsitektur ini memungkinkan fitur baru (modul) ditambahkan sebagai plugin **tanpa perlu mengompilasi ulang** aplikasi utama.

---

## 🏗️ Arsitektur

```
spring-modular-pf4j/
├── pom.xml                  # Parent POM (Maven Multi-Module)
├── plugins-api/             # Kontrak/API yang di-share antara core & plugin
├── core-app/                # Aplikasi utama Spring Boot
├── plugin-inventory/        # Plugin: Modul Inventory
├── plugin-order/            # Plugin: Modul Order (depends on inventory-plugin)
└── core-app/plugins/        # Folder untuk JAR plugin (runtime)
```

### Alur Kerja

```
┌─────────────┐      depends on      ┌──────────────┐
│  core-app   │ ─────────────────▶   │  plugins-api │
└──────┬──────┘                       └──────▲───────┘
       │ loads JAR from /plugins             │ depends on
       ▼                                     │
┌──────────────────┐    depends on    ┌──────────────────┐
│  plugin-order    │ ───────────────▶ │ plugin-inventory │
│   (JAR plugin)   │                  │   (JAR plugin)   │
└──────────────────┘                  └──────────────────┘
```

1. **`plugins-api`** — Berisi interface & model yang menjadi kontrak antara core dan plugin (extension points).
2. **`core-app`** — Aplikasi Spring Boot utama. Saat startup, ia memuat semua plugin JAR dari folder `plugins/`, menggabungkan classloader, lalu menjalankan Spring context.
3. **`plugin-inventory`** — Plugin yang menyediakan REST API untuk manajemen inventory. Mengimplementasi `InventoryCheckExtension` sehingga bisa dikonsumsi plugin lain.
4. **`plugin-order`** — Plugin yang menyediakan REST API untuk manajemen order. **Bergantung pada `inventory-plugin`** untuk validasi stok via `InventoryCheckExtension`. PF4J menjamin `inventory-plugin` distart terlebih dahulu.

---

## 🛠️ Tech Stack

| Komponen        | Versi              |
|-----------------|--------------------|
| Java            | 17                 |
| Spring Boot     | 3.4.0              |
| PF4J Spring     | 0.8.0              |
| Database        | H2 (file-based)    |
| Migration       | Flyway             |
| Build Tool      | Maven              |

---

## 🚀 Getting Started

### Prasyarat

- **Java 17+**
- **Maven 3.8+**

### Build

```bash
# Build semua module dari root project
mvn clean package -DskipTests
```

### Copy Plugin ke Folder `plugins/`

Setelah build, salin JAR plugin ke folder `plugins/` di dalam `core-app`:

```bash
mkdir -p core-app/plugins
cp plugin-inventory/target/plugin-inventory-1.0.0.jar core-app/plugins/
cp plugin-order/target/plugin-order-1.0.0.jar core-app/plugins/
```

> **💡 Catatan**: `plugin-order` bergantung pada `plugin-inventory`. PF4J akan otomatis mendeteksi dependensi ini dari `MANIFEST.MF` dan memastikan `inventory-plugin` distart lebih dulu.

### Jalankan Aplikasi

```bash
cd core-app
mvn spring-boot:run
```

Saat startup, Anda akan melihat output seperti ini:

```
==============================================
   MODULAR SYSTEM STARTUP CHECK               
==============================================
STATUS: Berhasil memuat 2 plugin.
----------------------------------------------
ID Plugin      : inventory-plugin
Versi          : 1.0.0
Class Utama    : com.example.inventory.InventoryModule
Status         : STARTED
----------------------------------------------
ID Plugin      : order-plugin
Versi          : 1.0.0
Class Utama    : com.example.order.OrderModule
Status         : STARTED
==============================================
```

---

## 📡 API Reference

### Core API

| Method | Endpoint       | Deskripsi                          |
|--------|----------------|-------------------------------------|
| GET    | `/api/health`  | Health check & daftar plugin aktif  |

**Contoh Response** `GET /api/health`:
```json
{
  "status": "UP",
  "app": "core-app",
  "plugins": [
    { "id": "inventory-plugin", "version": "1.0.0", "state": "STARTED" },
    { "id": "order-plugin",     "version": "1.0.0", "state": "STARTED" }
  ]
}
```

### Inventory Plugin API

| Method | Endpoint              | Deskripsi                |
|--------|-----------------------|--------------------------|
| GET    | `/api/inventory`      | Mendapatkan semua barang |
| POST   | `/api/inventory`      | Menambah barang baru     |
| GET    | `/api/inventory/{id}` | Mendapatkan barang by ID |
| DELETE | `/api/inventory/{id}` | Menghapus barang         |

**Contoh Request** `POST /api/inventory`:
```json
{
  "name": "Laptop",
  "quantity": 10,
  "price": 15000000
}
```

### Order Plugin API

| Method | Endpoint         | Deskripsi                                            |
|--------|------------------|------------------------------------------------------|
| GET    | `/api/orders`    | Mendapatkan semua order (dengan detail item)         |
| POST   | `/api/orders`    | Membuat order baru (validasi stok via inventory)     |

**Contoh Request** `POST /api/orders`:
```json
{
  "itemId": 1,
  "quantity": 2
}
```

**Contoh Response**:
```json
{
  "id": 1,
  "status": "APPROVED",
  "quantity": 2,
  "item": {
    "id": 1,
    "name": "Laptop",
    "quantity": 10,
    "price": 15000000
  }
}
```

> **💡 Catatan**: Jika stok tidak cukup, response akan `400 Bad Request` dengan status `"REJECTED"`.

---

## 🔌 Extension Points (`plugins-api`)

`plugins-api` mendefinisikan kontrak yang digunakan oleh core dan antar-plugin:

| Interface                  | Deskripsi                                                       |
|----------------------------|-----------------------------------------------------------------|
| `AppExtension`             | Info dasar plugin (nama, deskripsi)                            |
| `InventoryCheckExtension`  | Mengambil detail item inventory antar-plugin                   |
| `FlywayMigrationExtension` | Mendaftarkan lokasi migration Flyway dari setiap plugin        |
| `I18nExtension`            | Mendaftarkan lokasi pesan i18n (internasionalisasi) dari plugin |

---

## 🔗 Plugin Dependencies

`plugin-order` bergantung pada `plugin-inventory` untuk mengakses data inventory. Ini dikonfigurasi di `MANIFEST.MF`:

```
Plugin-Id: order-plugin
Plugin-Dependencies: inventory-plugin
```

PF4J akan **otomatis** memastikan `inventory-plugin` distart sebelum `order-plugin`.

### Pendekatan Komunikasi Antar-Plugin

`plugin-order` mengakses `plugin-inventory` melalui **interface** (`InventoryCheckExtension`), bukan melalui import langsung:

```java
// ✅ DIREKOMENDASIKAN — via interface di plugins-api (loose coupling)
List<InventoryCheckExtension> extensions =
    pluginManager.getExtensions(InventoryCheckExtension.class);
InventoryItemDTO item = extensions.get(0).getItemDetails(itemId);

// ❌ HINDARI — import langsung ke class plugin lain (tight coupling)
// import com.example.inventory.model.InventoryItem; ← JANGAN
```

---

## 🧪 Mode Standalone (Development)

Setiap plugin dapat dijalankan secara **standalone** untuk keperluan development tanpa harus menjalankan seluruh core app. Gunakan Maven profile `standalone`:

```bash
# Jalankan plugin-inventory standalone
cd plugin-inventory
mvn spring-boot:run -Pstandalone

# Jalankan plugin-order standalone
cd plugin-order
mvn spring-boot:run -Pstandalone
```

> **⚠️ Catatan**: Saat `plugin-order` berjalan standalone, `plugin-inventory` tidak tersedia sehingga detail item tidak akan muncul di response. `OrderService` akan otomatis skip pengecekan stok.

---

## ➕ Membuat Plugin Baru

### 1. Buat Module Maven Baru

Buat folder baru (contoh: `plugin-product/`) dan tambahkan `pom.xml`:

```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>modular-app</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>plugin-product</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>plugins-api</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>  <!-- disediakan oleh core-app saat runtime -->
        </dependency>
        <!-- Dependency Spring (provided, sudah ada di core-app) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

Tambahkan module di `pom.xml` root:
```xml
<modules>
    ...
    <module>plugin-product</module>
</modules>
```

### 2. Buat Plugin Class

```java
public class ProductModule extends Plugin {
    public ProductModule(PluginWrapper wrapper) {
        super(wrapper);}

    @Override
    public void start() {
        System.out.println(">>> Product Module: Memulai plugin...");
    }

    @Override
    public void stop() {
        System.out.println(">>> Product Module: Menghentikan plugin...");
    }
}
```

### 3. Konfigurasi MANIFEST.MF via `pom.xml`

Tambahkan konfigurasi `maven-jar-plugin` di `pom.xml` plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <configuration>
                <archive>
                    <manifestEntries>
                        <Plugin-Id>product-plugin</Plugin-Id>
                        <Plugin-Version>1.0.0</Plugin-Version>
                        <Plugin-Class>com.example.product.ProductModule</Plugin-Class>
                        <Plugin-Dependencies></Plugin-Dependencies>  <!-- kosong atau isi nama plugin lain -->
                    </manifestEntries>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 4. Tambah Entity, Repository, dan Controller

Buat komponen Spring seperti biasa — Core App akan otomatis mendeteksi `@Entity`, `@Repository`, dan `@RestController` dari plugin berkat `CompositeClassLoader`.

### 5. (Opsional) Tambah Migration Flyway

Buat file SQL di `src/main/resources/db/migration/` lalu daftarkan via `FlywayMigrationExtension`:

```java
@Extension
public class ProductFlywayExtension implements FlywayMigrationExtension {
    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration/product";
    }
}
```

### 6. Build & Deploy

```bash
mvn clean package -DskipTests
cp plugin-product/target/plugin-product-1.0.0.jar core-app/plugins/
# Restart core-app
```

---

## ⚙️ Konfigurasi

Konfigurasi utama ada di `core-app/src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database (H2 file-based)
spring.datasource.url=jdbc:h2:file:./data/modular_db;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Hibernate auto DDL
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Allow bean override dari plugin
spring.main.allow-bean-definition-overriding=true
```

> **💡 Tip**: Untuk production, ganti H2 dengan MySQL/PostgreSQL dan ubah `ddl-auto` menjadi `validate` atau `none`.

---

## 📁 Struktur Detail

```
spring-modular-pf4j/
│
├── plugins-api/
│   └── src/main/java/com/example/api/
│       ├── AppExtension.java               # Extension point dasar
│       ├── InventoryCheckExtension.java    # Kontrak akses data inventory
│       ├── FlywayMigrationExtension.java   # Kontrak registrasi migration
│       ├── I18nExtension.java              # Kontrak registrasi pesan i18n
│       ├── InventoryItemDTO.java           # Shared DTO untuk data inventory
│       └── model/
│           └── ModuleInfo.java             # Model info plugin
│
├── core-app/
│   └── src/main/java/com/example/core/
│       ├── CoreApplication.java            # Main class + plugin loading
│       ├── classloader/
│       │   └── CompositeClassLoader.java   # Gabungan classloader
│       ├── config/
│       │   ├── PluginConfig.java           # Konfigurasi PF4J PluginManager
│       │   ├── PluginFlywayConfig.java     # Agregasi migration dari plugin
│       │   └── PluginMessageSourceConfig.java  # Agregasi pesan i18n
│       └── controller/
│           └── CoreController.java         # Health check endpoint
│
├── plugin-inventory/
│   └── src/main/java/com/example/inventory/
│       ├── InventoryModule.java            # Plugin entry point
│       ├── InventoryExtension.java         # Implementasi AppExtension
│       ├── InventoryCheckExtensionImpl.java # Implementasi InventoryCheckExtension
│       ├── InventoryStandaloneApp.java     # Entry point mode standalone
│       ├── controller/
│       │   └── InventoryController.java    # REST API inventory
│       ├── model/
│       │   └── InventoryItem.java          # JPA Entity
│       └── repository/
│           └── InventoryRepository.java    # Spring Data JPA
│
├── plugin-order/
│   └── src/main/java/com/example/order/
│       ├── OrderModule.java                # Plugin entry point
│       ├── OrderExtension.java             # Implementasi AppExtension
│       ├── OrderStandaloneApp.java         # Entry point mode standalone
│       ├── controller/
│       │   └── OrderController.java        # REST API order
│       ├── dto/
│       │   └── OrderResponse.java          # Response DTO
│       ├── model/
│       │   └── Order.java                  # JPA Entity
│       ├── repository/
│       │   └── OrderRepository.java        # Spring Data JPA
│       └── service/
│           └── OrderService.java           # Business logic
│
└── pom.xml                                 # Parent POM
```

---

## 📝 Lisensi

Project ini dibuat untuk tujuan pembelajaran dan pengembangan.
