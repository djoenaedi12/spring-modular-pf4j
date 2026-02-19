# Spring Modular PF4J

Aplikasi **Spring Boot modular** yang menggunakan [PF4J](https://pf4j.org/) sebagai plugin framework. Arsitektur ini memungkinkan fitur baru (modul) ditambahkan sebagai plugin **tanpa perlu mengompilasi ulang** aplikasi utama.

---

## 🏗️ Arsitektur

```
spring-modular-pf4j/
├── pom.xml                  # Parent POM (Maven Multi-Module)
├── plugins-api/             # Kontrak/API yang di-share antara core & plugin
├── core-app/                # Aplikasi utama Spring Boot
├── plugin-inventory/        # Contoh plugin: Modul Inventory
└── plugins/                 # Folder untuk JAR plugin (runtime)
```

### Alur Kerja

```
┌─────────────┐      depends on      ┌──────────────┐
│  core-app   │ ──────────────────▶   │  plugins-api │
└──────┬──────┘                       └──────▲───────┘
       │ loads JAR from /plugins             │ depends on
       ▼                                     │
┌──────────────────┐                         │
│ plugin-inventory │ ────────────────────────┘
│   (JAR plugin)   │
└──────────────────┘
```

1. **`plugins-api`** — Berisi interface & model yang menjadi kontrak antara core dan plugin.
2. **`core-app`** — Aplikasi Spring Boot utama. Saat startup, ia memuat semua plugin JAR dari folder `plugins/`, menggabungkan classloader, lalu menjalankan Spring context.
3. **`plugin-inventory`** — Contoh plugin yang menyediakan REST API untuk manajemen inventory.

---

## 🛠️ Tech Stack

| Komponen        | Versi   |
|-----------------|---------|
| Java            | 17      |
| Spring Boot     | 3.4.0   |
| PF4J Spring     | 0.8.0   |
| Database        | H2 (file-based) |
| Build Tool      | Maven   |

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
```

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
STATUS: Berhasil memuat 1 plugin.
----------------------------------------------
ID Plugin      : inventory-plugin
Versi          : 1.0.0
Class Utama    : com.example.inventory.InventoryModule
Status         : STARTED
==============================================
```

---

## 📡 API Reference

### Core API

| Method | Endpoint       | Deskripsi                          |
|--------|----------------|------------------------------------|
| GET    | `/api/health`  | Health check & daftar plugin aktif |

**Contoh Response** `GET /api/health`:
```json
{
  "status": "UP",
  "app": "core-app",
  "plugins": [
    {
      "id": "inventory-plugin",
      "version": "1.0.0",
      "state": "STARTED"
    }
  ]
}
```

### Inventory Plugin API

| Method | Endpoint              | Deskripsi                  |
|--------|-----------------------|----------------------------|
| GET    | `/api/inventory`      | Mendapatkan semua barang   |
| POST   | `/api/inventory`      | Menambah barang baru       |
| GET    | `/api/inventory/{id}` | Mendapatkan barang by ID   |
| DELETE | `/api/inventory/{id}` | Menghapus barang           |

**Contoh Request** `POST /api/inventory`:
```json
{
  "name": "Laptop",
  "quantity": 10,
  "price": 15000000
}
```

---

## 🔌 Membuat Plugin Baru

### 1. Buat Module Maven Baru

Buat folder baru (contoh: `plugin-order/`) dan tambahkan `pom.xml`:

```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>modular-app</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>plugin-order</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>plugins-api</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- Tambahkan dependency Spring yang diperlukan -->
    </dependencies>
</project>
```

### 2. Buat Plugin Class

```java
public class OrderModule extends Plugin {
    public OrderModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println(">>> Order Module: Started!");
    }

    @Override
    public void stop() {
        System.out.println(">>> Order Module: Stopped!");
    }
}
```

### 3. Tambahkan `MANIFEST.MF`

Buat file `src/main/resources/META-INF/MANIFEST.MF`:

```
Plugin-Id: order-plugin
Plugin-Version: 1.0.0
Plugin-Class: com.example.order.OrderModule
Plugin-Provider: Your Name
Plugin-Dependencies:
```

### 4. Tambah Entity, Repository, dan Controller

Buat komponen Spring seperti biasa — Core App akan otomatis mendeteksi `@Entity`, `@Repository`, dan `@RestController` dari plugin berkat `CompositeClassLoader`.

### 5. Build & Deploy

```bash
mvn clean package -DskipTests
cp plugin-order/target/plugin-order-1.0.0.jar core-app/plugins/
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
│       ├── AppExtension.java          # Extension point interface
│       └── model/                     # Shared models
│
├── core-app/
│   └── src/main/java/com/example/core/
│       ├── CoreApplication.java       # Main class + plugin loading
│       ├── classloader/
│       │   └── CompositeClassLoader.java  # Gabungan classloader
│       ├── config/                    # Konfigurasi Spring
│       └── controller/
│           └── CoreController.java    # Health check endpoint
│
├── plugin-inventory/
│   └── src/main/java/com/example/inventory/
│       ├── InventoryModule.java       # Plugin entry point
│       ├── controller/
│       │   └── InventoryController.java   # REST API
│       ├── model/
│       │   └── InventoryItem.java     # JPA Entity
│       └── repository/
│           └── InventoryRepository.java   # Spring Data JPA
│
└── pom.xml                            # Parent POM
```

---

## 📝 Lisensi

Project ini dibuat untuk tujuan pembelajaran dan pengembangan.
