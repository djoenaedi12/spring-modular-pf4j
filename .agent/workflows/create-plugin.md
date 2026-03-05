---
description: How to create and develop a new plugin module
---

# Membuat Plugin Baru

## 1. Buat Maven Module

Buat folder `<nama>-plugin/` di root project, lalu buat `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>gasi.gps</groupId>
        <artifactId>modular-app</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId><nama>-plugin</artifactId>

    <properties>
        <plugin.id><nama>-plugin</plugin.id>
        <plugin.class>gasi.gps.<nama>.<Nama>Plugin</plugin.class>
        <plugin.version>1.0.0</plugin.version>
        <plugin.provider>GASI</plugin.provider>
        <plugin.description>Deskripsi plugin</plugin.description>
        <plugin.dependencies></plugin.dependencies>
    </properties>

    <dependencies>
        <dependency>
            <groupId>gasi.gps</groupId>
            <artifactId>core-api</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Plugin-Id>${plugin.id}</Plugin-Id>
                            <Plugin-Version>${plugin.version}</Plugin-Version>
                            <Plugin-Class>${plugin.class}</Plugin-Class>
                            <Plugin-Provider>${plugin.provider}</Plugin-Provider>
                            <Plugin-Description>${plugin.description}</Plugin-Description>
                            <Plugin-Dependencies>${plugin.dependencies}</Plugin-Dependencies>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-checkstyle-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>com.github.spotbugs</groupId>
                <artifactId>spotbugs-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Daftarkan module di root `pom.xml`:

```xml
<modules>
    ...
    <module><nama>-plugin</module>
</modules>
```

## 2. Buat Boilerplate Classes

Buat 3 file wajib di `<nama>-plugin/src/main/java/gasi/gps/<nama>/`:

### `<Nama>Plugin.java` — PF4J plugin class

```java
package gasi.gps.<nama>;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class <Nama>Plugin extends Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(<Nama>Plugin.class);

    @Override
    public void start() {
        LOG.info("<Nama> plugin started");
    }

    @Override
    public void stop() {
        LOG.info("<Nama> plugin stopped");
    }
}
```

### `<Nama>AppExtension.java` — metadata extension

```java
package gasi.gps.<nama>;

import org.pf4j.Extension;
import gasi.gps.core.api.AppExtension;

@Extension
public class <Nama>AppExtension implements AppExtension {
    @Override
    public String getModuleName() { return "<nama>-plugin"; }

    @Override
    public String getModuleDescription() { return "Deskripsi plugin."; }

    @Override
    public String getModuleVersion() { return "1.0.0"; }
}
```

### `<Nama>FlywayExtension.java` — database migration

```java
package gasi.gps.<nama>;

import org.pf4j.Extension;
import gasi.gps.core.api.infrastructure.FlywayMigrationExtension;

@Extension
public class <Nama>FlywayExtension implements FlywayMigrationExtension {
    @Override
    public String getMigrationLocation() {
        return "classpath:db/migration";
    }
}
```

## 3. Buat Package Structure

```
<nama>-plugin/src/main/java/gasi/gps/<nama>/
├── <Nama>Plugin.java
├── <Nama>AppExtension.java
├── <Nama>FlywayExtension.java
├── application/
│   ├── dto/            # Request/Response DTO
│   ├── mapper/         # DTO ↔ Domain mappers
│   └── service/        # Service implementations (use cases)
├── domain/
│   ├── model/          # Domain models (extends BaseModel)
│   └── port/
│       ├── inbound/    # Service interfaces
│       └── outbound/   # Repository port interfaces (extends BaseRepositoryPort)
├── infrastructure/
│   ├── adapter/        # Repository adapters (extends BaseRepositoryAdapter)
│   ├── entity/         # JPA entities (extends BaseEntity)
│   ├── mapper/         # Entity ↔ Domain mappers (MapStruct)
│   └── persistence/    # JPA repository interfaces (extends JpaRepository)
└── presentation/
    └── controller/     # REST controllers
```

## 4. Buat Flyway Migration

Buat file SQL di `<nama>-plugin/src/main/resources/db/migration/`:

```
V1__create_<nama>_tables.sql
```

## 5. Development & Debug

### Tambahkan plugin ke core-app (development only)

Di `core-app/pom.xml`, tambahkan dependency sementara:

```xml
<!-- DEV ONLY -->
<dependency>
    <groupId>gasi.gps</groupId>
    <artifactId><nama>-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

Tambahkan juga plugin lain yang dibutuhkan, misal `auth-plugin`.

### Run & Debug

// turbo
1. Run `CoreApplication.main()` dari IDE
2. Breakpoint bisa langsung dipasang di code plugin
3. Tidak perlu build JAR — Spring component scan otomatis dari classpath

### Plugin yang ikut running

Hanya plugin yang ada di `core-app/pom.xml` dependencies yang akan running. Plugin lain yang tidak di-declare **tidak akan di-load**.

## 6. Build untuk Production

// turbo
1. Run `mvn clean package -pl <nama>-plugin`
2. Copy JAR ke folder `plugins/`
3. Hapus dev dependency dari `core-app/pom.xml`
