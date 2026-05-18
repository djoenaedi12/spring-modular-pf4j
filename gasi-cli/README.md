# gasi

Developer toolkit untuk modular Spring Boot + PF4J project. CLI ini menyediakan command untuk:

- scaffold, build, deploy, clean, list, dan delete plugin
- generate dan delete resource CRUD di dalam plugin
- generate upload processor untuk shared data upload workflow
- generate resource secara interactive atau dari file JSON
- mengatur field yang masuk ke DTO create/update/summary/detail
- menambahkan basic validation annotation dari resource spec

## Install

```bash
cd gasi
npm install
npm link            # registers `gasi` command globally
```

## Usage

### Plugin create — interactive

```bash
cd /path/to/spring-modular-pf4j
gasi plugin create
```

Akan ditanya: nama plugin, domain, plugin prefix, base package, versi, deskripsi, dependency plugin, lalu konfirmasi.

`plugin prefix` bersifat optional. Jika diisi, value ini akan ditulis ke `plugin.properties` sebagai:

```properties
plugin.prefix=auth
```

Prefix ini bisa dipakai oleh command `resource create` sebagai prefix nama table database.

Contoh:

```text
plugin.prefix=auth
Employee -> auth_employees
```

Jika prefix kosong:

```text
plugin.prefix=
Employee -> employees
```

### Plugin create — non-interactive

```bash
gasi plugin create \
  --name payroll \
  --domain payroll \
  --plugin-prefix pay \
  --package gasi.gps \
  --plugin-version 1.0.0 \
  --description "Payroll plugin" \
  --yes
```

Jika tidak ingin memakai table prefix:

```bash
gasi plugin create \
  --name employee \
  --domain employee \
  --package gasi.gps \
  --plugin-version 1.0.0 \
  --description "Employee plugin" \
  --yes
```

Hasil `plugin.properties`:

```properties
plugin.id=employee-plugin
plugin.prefix=
plugin.class=gasi.gps.employee.EmployeePlugin
plugin.version=1.0.0
plugin.provider=GASI
plugin.description=Employee plugin
plugin.dependencies=
```

### Plugin create flags

| Flag                   | Deskripsi                                                |
|------------------------|----------------------------------------------------------|
| `-n, --name`           | Nama plugin tanpa suffix `-plugin`, mis. `payroll`       |
| `-d, --domain`         | Nama Java sub-package, mis. `payroll`                    |
| `--plugin-prefix`      | Optional table prefix untuk plugin, mis. `auth`          |
| `-p, --package`        | Base package, default: `gasi.gps`                        |
| `-v, --plugin-version` | Versi plugin, default: `1.0.0`                           |
| `--description`        | Deskripsi plugin                                         |
| `--depends-on`         | Plugin dependency PF4J. Bisa diulang atau comma-separated |
| `--no-flyway`          | Skip Flyway migration sample                             |
| `--no-register`        | Skip auto-register di parent `pom.xml`                   |
| `-y, --yes`            | Skip interactive prompts                                 |
| `--cwd <path>`         | Root project, default: current directory                 |

## Yang Di-generate oleh Plugin Create

```text
plugins/{name}-plugin/
├── pom.xml                                  # parent inherit + PF4J manifest entries
└── src/main/
    ├── java/{base-package}/{domain}/
    │   ├── {Name}Plugin.java                # PF4J Plugin entry
    │   └── extension/
    │       ├── {Name}AppExtension.java
    │       ├── {Name}FlywayMigrationExtension.java
    │       └── {Name}I18nExtension.java
    └── resources/
        ├── plugin.properties                # PF4J descriptor
        ├── db/migration/{name}/
        │   └── V{timestamp}__init_{name}_schema.sql    # skip jika --no-flyway
        └── i18n/{name}/
            └── messages.properties
```

Folder Clean Architecture layer seperti `application/dto`, `domain/model`, `infrastructure/entity`, dan sejenisnya tidak dibuat oleh `plugin create`. Folder dan file resource dibuat oleh `gasi resource create`.

## Plugin Dependencies (`Plugin-Dependencies` di PF4J)

Plugin bisa depend ke plugin lain. Berbeda dari Maven dependency, ini adalah metadata PF4J yang dipakai untuk classloader dan load order, lalu ditulis ke manifest JAR.

**Format**: `<plugin-id>[@<version-constraint>][?]`

| Input                       | Arti                                  |
|-----------------------------|---------------------------------------|
| `auth-plugin`               | required, any version                 |
| `auth-plugin@1.0.0`         | required, exact version               |
| `auth-plugin@>=1.0.0`       | required, version constraint          |
| `auth-plugin?`              | optional, any version                 |
| `auth-plugin@>=1.0.0?`      | optional + version constraint         |

**Cara input:**

```bash
# Single dependency
gasi plugin create -n audit --depends-on auth-plugin

# Multiple flags
gasi plugin create -n audit --depends-on auth-plugin --depends-on 'ldap-plugin?'

# Comma-separated dalam satu flag
gasi plugin create -n audit --depends-on 'auth-plugin,ldap-plugin@>=1.0.0?'

# Interactive: CLI scan plugin yang sudah terdaftar di parent pom dan tampilkan multi-select
gasi plugin create
```

Di interactive mode, CLI membaca `<modules>` di parent `pom.xml`, mengambil module yang berakhiran `-plugin`, lalu menampilkannya via multi-select. Ada opsi tambah manual untuk memasukkan dependency yang belum ada di parent pom atau dependency yang butuh version constraint / optional marker.

Jika plugin yang di-depend tidak ada di parent pom, CLI menampilkan warning tapi tetap lanjut generate. PF4J akan gagal load saat runtime jika dependency tersebut benar-benar tidak tersedia.

Output di `pom.xml` plugin:

```xml
<plugin.dependencies>auth-plugin, ldap-plugin@>=1.0.0?</plugin.dependencies>
```

Value tersebut kemudian masuk ke `Plugin-Dependencies` di manifest JAR via `${plugin.dependencies}`.

## Auto-register

Jika `--no-register` tidak diset, CLI akan menambahkan module plugin ke blok `<modules>` di parent `pom.xml`.

Contoh module yang ditambahkan:

```xml
<module>plugins/payroll-plugin</module>
```

Proses ini idempotent. Kalau module sudah ada, akan di-skip.

## Plugin Lifecycle Commands

### List plugins

```bash
gasi plugin list
```

Menampilkan plugin modules yang terdaftar di parent `pom.xml`.

### Build plugin

```bash
gasi plugin build auth
gasi plugin build auth-plugin --skip-tests
gasi plugin build auth --profile dev
```

Command ini menjalankan Maven dari project root. Jika struktur plugin ada di folder `plugins/`, command Maven yang dijalankan kira-kira:

```bash
mvn clean package -pl plugins/auth-plugin -am
```

Useful flags:

| Flag             | Description                                      |
|------------------|--------------------------------------------------|
| `--skip-tests`   | Adds `-DskipTests` to the Maven command          |
| `--profile`      | Adds `-P<name>` to the Maven command             |
| `--dry-run`      | Prints the Maven command without running it      |
| `--verbose`      | Shows full Maven output                          |
| `--cwd <path>`   | Root project, default: current directory         |

### Deploy plugin

```bash
gasi plugin deploy auth
gasi plugin deploy auth --plugins-dir platform-app/plugins
gasi plugin deploy auth --keep-old
```

Mencari main JAR di `{plugin}/target/`, lalu menyalinnya ke `platform-app/plugins/`. JAR `sources`, `javadoc`, dan `original-*` akan di-skip.

Useful flags:

| Flag                   | Description                                         |
|------------------------|-----------------------------------------------------|
| `--plugins-dir <path>` | Plugin deployment directory                         |
| `--keep-old`           | Keep older deployed JARs for the same plugin        |
| `--dry-run`            | Prints deploy actions without changing files        |
| `--cwd <path>`         | Root project, default: current directory            |

### Clean deployed plugin

```bash
gasi plugin clean auth
gasi plugin clean auth --plugins-dir platform-app/plugins
```

Menghapus deployed JAR untuk plugin yang dipilih dari plugins directory. Command ini tidak menghapus source module plugin atau folder `target/`.

### Delete plugin

```bash
gasi plugin delete payroll
gasi plugin delete payroll --dry-run
gasi plugin delete payroll --yes
```

Menghapus source module plugin, menghapus entry `<module>` dari parent `pom.xml`, dan menghapus deployed JAR untuk plugin tersebut dari `platform-app/plugins/`. Command ini meminta konfirmasi kecuali `--yes` diberikan.

Useful flags:

| Flag                   | Description                                         |
|------------------------|-----------------------------------------------------|
| `--plugins-dir <path>` | Plugin deployment directory                         |
| `--keep-deployed`      | Keep deployed JARs in the plugins directory         |
| `--dry-run`            | Prints delete actions without changing files        |
| `-y, --yes`            | Skips the confirmation prompt                       |
| `--cwd <path>`         | Root project, default: current directory            |

## Uploader Commands

Uploader command dipakai untuk generate processor resource-specific untuk shared
upload API. Controller dan service upload tetap memakai base upload dari core.

### Uploader create

```bash
gasi uploader create Employee
```

Jika dijalankan dari dalam folder plugin, target plugin otomatis dideteksi. Jika
dijalankan dari root project, CLI akan meminta target plugin.

Non-interactive:

```bash
gasi uploader create Employee --plugin employee --yes
```

Custom resource path:

```bash
gasi uploader create Employee --resource employee-master --plugin employee --yes
```

Hasil utama:

```text
plugins/{plugin}-plugin/src/main/java/{package}/application/service/EmployeeUplProcessor.java
```

Processor yang digenerate:

- register resource melalui `resource()`
- membaca file lewat `FileReaderRegistry`
- mengisi `DataRowUpl` dari `FileRow`
- menyediakan `validateRows` dan `commitRows` untuk diisi sesuai kebutuhan resource

## Resource Commands

Resource command dipakai untuk generate file CRUD resource di dalam plugin yang sudah ada.

### Resource create — interactive

Jalankan dari dalam folder plugin atau dari project root.

```bash
gasi resource create Employee
```

Jika dijalankan dari dalam folder plugin, CLI akan mendeteksi plugin secara otomatis.

Jika dijalankan dari project root, CLI akan menampilkan pilihan target plugin.

Pada mode interactive, setiap field akan ditanya:

- nama field
- tipe field
- konfigurasi khusus tipe, misalnya `length`, `enumName`, atau `refEntity`
- `required`
- `unique` jika tipe field mendukung
- `filterable`
- DTO inclusion
- extra validation

DTO inclusion menggunakan checkbox dan default-nya semua tercentang:

```text
Include "employeeNo" in DTOs:
 ◉ Create Request
 ◉ Update Request
 ◉ Summary Response
 ◉ Detail Response
```

User bisa uncheck DTO yang tidak membutuhkan field tersebut.

### Resource create — dari file JSON

```bash
gasi resource create --file resources/employee.resource.json
```

Atau tetap boleh memberi entity name dari CLI sebagai fallback:

```bash
gasi resource create Employee --file resources/employee.resource.json
```

Jika JSON sudah punya `entityName`, value dari JSON yang dipakai.

### Resource create — multiple file

`--file` bisa dipakai lebih dari sekali.

```bash
gasi resource create \
  --file resources/employee.resource.json \
  --file resources/department.resource.json
```

### Resource create flags

| Flag             | Deskripsi                                               |
|------------------|---------------------------------------------------------|
| `entityName`     | Optional jika memakai `--file`; wajib jika interactive  |
| `-f, --file`     | File definisi resource JSON. Bisa diulang               |
| `-y, --yes`      | Skip confirmation prompt                                |
| `--cwd <path>`   | Root project, default: current directory                |

### Format JSON: single resource

```json
{
  "entityName": "Employee",
  "fields": [
    {
      "name": "employeeNo",
      "type": "String",
      "length": 50,
      "required": true,
      "unique": true
    },
    {
      "name": "fullName",
      "type": "String",
      "length": 150,
      "required": true
    },
    {
      "name": "status",
      "type": "Enum",
      "enumName": "EmployeeStatus",
      "required": true
    },
    {
      "name": "department",
      "type": "ManyToOne",
      "refEntity": "Department",
      "required": true
    }
  ]
}
```

### Format JSON: multiple resources

```json
{
  "resources": [
    {
      "entityName": "Employee",
      "fields": [
        {
          "name": "employeeNo",
          "type": "String",
          "length": 50,
          "required": true,
          "unique": true
        },
        {
          "name": "fullName",
          "type": "String",
          "length": 150,
          "required": true
        }
      ]
    },
    {
      "entityName": "Department",
      "fields": [
        {
          "name": "code",
          "type": "String",
          "length": 50,
          "required": true,
          "unique": true
        },
        {
          "name": "name",
          "type": "String",
          "length": 150,
          "required": true
        }
      ]
    }
  ]
}
```

### Parent / child resource

Resource bisa dibuat sebagai child dari resource lain dengan menambahkan `parent`.
Format field tetap sama seperti resource biasa; CLI hanya menambahkan relasi parent
secara otomatis.

```json
{
  "resources": [
    {
      "entityName": "Role",
      "fields": [
        {
          "name": "code",
          "type": "String",
          "length": 50,
          "required": true,
          "unique": true
        },
        {
          "name": "name",
          "type": "String",
          "length": 150,
          "required": true
        }
      ]
    },
    {
      "entityName": "Permission",
      "parent": "Role",
      "as": "permissions",
      "fields": [
        {
          "name": "action",
          "type": "ManyToOne",
          "refEntity": "Action",
          "required": true
        },
        {
          "name": "resource",
          "type": "ManyToOne",
          "refEntity": "Resource",
          "required": true
        }
      ]
    },
    {
      "entityName": "RoleMenu",
      "parent": "Role",
      "as": "menuIds",
      "fields": [
        {
          "name": "menu",
          "type": "ManyToOne",
          "refEntity": "Menu",
          "required": true
        }
      ]
    }
  ]
}
```

Rule default:

| Property | Default | Keterangan |
|----------|---------|------------|
| `parent` | - | Jika diisi, resource dianggap child |
| `as` | plural dari `entityName` | Nama field child saat parent wiring ditambahkan |
| `exposeApi` | `true` untuk root, `false` untuk child | Jika `false`, controller, service API, DTO CRUD, dan inbound port tidak dibuat |

Untuk child, CLI tetap membuat domain model, JPA entity, repository port,
repository adapter, mapper, repository JPA, dan migration. Field parent otomatis
ditambahkan sebagai `ManyToOne`, misalnya `Permission.parent = Role` menghasilkan
field `role`. Wiring ke parent DTO/service bisa ditambahkan setelah resource
tergenerate sesuai kebutuhan aggregate.

### Format JSON: array root

Selain object dengan property `resources`, root JSON juga boleh langsung berupa array.

```json
[
  {
    "entityName": "Employee",
    "fields": [
      {
        "name": "employeeNo",
        "type": "String",
        "length": 50,
        "required": true,
        "unique": true
      }
    ]
  },
  {
    "entityName": "Department",
    "fields": [
      {
        "name": "code",
        "type": "String",
        "length": 50,
        "required": true,
        "unique": true
      }
    ]
  }
]
```

### Field types

| Type         | Keterangan                                              |
|--------------|----------------------------------------------------------|
| `String`     | Text pendek, mendukung `length`                          |
| `Text`       | Text panjang, biasanya `TEXT`                            |
| `MediumText` | Text panjang MySQL/MariaDB `MEDIUMTEXT`                  |
| `Integer`    | Integer number                                           |
| `Long`       | Long number                                              |
| `BigDecimal` | Decimal number                                           |
| `Double`     | Double number                                            |
| `Boolean`    | Boolean                                                  |
| `Date`       | Date                                                     |
| `DateTime`   | Date and time                                            |
| `Instant`    | Instant timestamp                                        |
| `Enum`       | Enum Java, wajib isi `enumName`                          |
| `ManyToOne`  | Relasi Many-to-One, wajib isi `refEntity`                |

### Field properties

| Property     | Wajib | Keterangan                                           |
|--------------|-------|------------------------------------------------------|
| `name`       | Ya    | Nama field camelCase                                 |
| `type`       | Ya    | Salah satu field type yang didukung                  |
| `length`     | Tidak | Untuk `String`, default `255` jika tidak diisi       |
| `required`   | Tidak | Default `true`                                       |
| `unique`     | Tidak | Hanya untuk `String`, `Integer`, dan `Long`          |
| `filterable` | Tidak | Default `true`; menentukan field masuk filter/search |
| `dto`        | Tidak | Mengatur field masuk DTO create/update/summary/detail |
| `validation` | Tidak | Extra validation annotation untuk DTO request        |
| `enumName`   | Untuk `Enum` | Nama enum PascalCase, mis. `EmployeeStatus` |
| `refEntity`  | Untuk `ManyToOne` | Nama entity tujuan PascalCase, mis. `Department` |

Untuk field normal `ManyToOne`, DTO request memakai encoded ID, misalnya field
`department` menjadi `departmentId`. Generated service akan me-resolve ID
tersebut melalui `{RefEntity}RepositoryPort` dengan `ReferenceResolver`,
mengumpulkan semua reference error, lalu mengisi relasi domain sebelum save atau
update.

## DTO Inclusion

Tidak semua field entity harus masuk ke semua DTO.

Generator mendukung konfigurasi DTO per field melalui property `dto`.

```json
{
  "name": "employeeNo",
  "type": "String",
  "length": 50,
  "required": true,
  "unique": true,
  "dto": {
    "create": true,
    "update": false,
    "summary": true,
    "detail": true
  }
}
```

Hasilnya:

```text
EmployeeCreateRequest   -> employeeNo masuk
EmployeeUpdateRequest   -> employeeNo tidak masuk
EmployeeSummaryResponse -> employeeNo masuk
EmployeeDetailResponse  -> employeeNo masuk
```

Jika `dto` tidak diisi, default-nya semua `true`:

```json
{
  "name": "fullName",
  "type": "String"
}
```

Sama dengan:

```json
{
  "name": "fullName",
  "type": "String",
  "dto": {
    "create": true,
    "update": true,
    "summary": true,
    "detail": true
  }
}
```

Jika `dto` hanya diisi sebagian, property lain dianggap `true`.

Contoh:

```json
{
  "name": "employeeNo",
  "type": "String",
  "dto": {
    "update": false
  }
}
```

Artinya:

```text
create  = true
update  = false
summary = true
detail  = true
```

DTO keys yang didukung:

| Key       | DTO yang terpengaruh             |
|-----------|----------------------------------|
| `create`  | `{Entity}CreateRequest`          |
| `update`  | `{Entity}UpdateRequest`          |
| `summary` | `{Entity}SummaryResponse`        |
| `detail`  | `{Entity}DetailResponse`         |

## Filterable Field

Property `filterable` menentukan apakah field dapat dimasukkan ke filter/search generated resource.

```json
{
  "name": "fullName",
  "type": "String",
  "length": 150,
  "filterable": true
}
```

Jika tidak diisi, default-nya `true`.

Gunakan `false` untuk field yang tidak perlu difilter, misalnya field text panjang:

```json
{
  "name": "notes",
  "type": "MediumText",
  "filterable": false
}
```

## Extra Validation

Selain validasi dasar dari `required` dan `length`, generator mendukung extra validation melalui property `validation`.

Validasi dasar:

| Source              | Annotation yang biasanya dibuat              |
|---------------------|-----------------------------------------------|
| `required: true` pada `String/Text/MediumText` | `@NotBlank`              |
| `required: true` pada tipe non-string          | `@NotNull`               |
| `length` pada `String`                         | `@Size(max = length)`    |

Extra validation tergantung tipe field.

### String / Text / MediumText validation

| Key              | Annotation                       |
|------------------|----------------------------------|
| `email`          | `@Email`                         |
| `minLength`      | `@Size(min = ...)`               |
| `maxLength`      | `@Size(max = ...)`               |
| `pattern`        | `@Pattern(regexp = "...")`      |
| `patternMessage` | message untuk `@Pattern`         |

Contoh:

```json
{
  "name": "email",
  "type": "String",
  "length": 150,
  "required": true,
  "validation": {
    "email": true,
    "minLength": 5,
    "maxLength": 150
  }
}
```

Contoh pattern:

```json
{
  "name": "employeeNo",
  "type": "String",
  "length": 50,
  "validation": {
    "pattern": "^[A-Z0-9_-]+$",
    "patternMessage": "Employee number may only contain uppercase letters, numbers, underscore, or dash"
  }
}
```

### Integer / Long validation

| Key              | Annotation          |
|------------------|---------------------|
| `min`            | `@Min`              |
| `max`            | `@Max`              |
| `positive`       | `@Positive`         |
| `positiveOrZero` | `@PositiveOrZero`   |
| `negative`       | `@Negative`         |
| `negativeOrZero` | `@NegativeOrZero`   |

Contoh:

```json
{
  "name": "age",
  "type": "Integer",
  "validation": {
    "min": 18,
    "max": 60
  }
}
```

### BigDecimal / Double validation

| Key              | Annotation          |
|------------------|---------------------|
| `decimalMin`     | `@DecimalMin`       |
| `decimalMax`     | `@DecimalMax`       |
| `digits`         | `@Digits`           |
| `positive`       | `@Positive`         |
| `positiveOrZero` | `@PositiveOrZero`   |
| `negative`       | `@Negative`         |
| `negativeOrZero` | `@NegativeOrZero`   |

Contoh:

```json
{
  "name": "salary",
  "type": "BigDecimal",
  "validation": {
    "decimalMin": "0.00",
    "decimalMax": "999999999.99",
    "digits": {
      "integer": 12,
      "fraction": 2
    }
  }
}
```

### Date / DateTime / Instant validation

| Key               | Annotation             |
|-------------------|------------------------|
| `past`            | `@Past`                |
| `pastOrPresent`   | `@PastOrPresent`       |
| `future`          | `@Future`              |
| `futureOrPresent` | `@FutureOrPresent`     |

Contoh:

```json
{
  "name": "effectiveDate",
  "type": "Date",
  "validation": {
    "futureOrPresent": true
  }
}
```

### Boolean validation

| Key           | Annotation       |
|---------------|------------------|
| `assertTrue`  | `@AssertTrue`    |
| `assertFalse` | `@AssertFalse`   |

Contoh:

```json
{
  "name": "accepted",
  "type": "Boolean",
  "validation": {
    "assertTrue": true
  }
}
```

### Catatan kombinasi validation

Beberapa validation bisa digabung, misalnya:

```json
{
  "validation": {
    "minLength": 3,
    "maxLength": 50,
    "pattern": "^[a-zA-Z0-9_]+$"
  }
}
```

Namun beberapa validation bersifat eksklusif:

| Type        | Eksklusif |
|-------------|-----------|
| Numeric     | Pilih salah satu dari `positive`, `positiveOrZero`, `negative`, `negativeOrZero` |
| Date        | Pilih salah satu dari `past`, `pastOrPresent`, `future`, `futureOrPresent` |
| Boolean     | Pilih salah satu dari `assertTrue`, `assertFalse` |

## Contoh JSON lengkap

```json
{
  "entityName": "Employee",
  "fields": [
    {
      "name": "employeeNo",
      "type": "String",
      "length": 50,
      "required": true,
      "unique": true,
      "filterable": true,
      "dto": {
        "update": false
      },
      "validation": {
        "minLength": 3,
        "maxLength": 50,
        "pattern": "^[A-Z0-9_-]+$",
        "patternMessage": "Employee number may only contain uppercase letters, numbers, underscore, or dash"
      }
    },
    {
      "name": "fullName",
      "type": "String",
      "length": 150,
      "required": true,
      "filterable": true,
      "validation": {
        "minLength": 3,
        "maxLength": 150
      }
    },
    {
      "name": "email",
      "type": "String",
      "length": 150,
      "required": false,
      "filterable": true,
      "dto": {
        "summary": false
      },
      "validation": {
        "email": true,
        "maxLength": 150
      }
    },
    {
      "name": "notes",
      "type": "MediumText",
      "required": false,
      "filterable": false,
      "dto": {
        "create": true,
        "update": true,
        "summary": false,
        "detail": true
      }
    },
    {
      "name": "salary",
      "type": "BigDecimal",
      "required": false,
      "filterable": false,
      "validation": {
        "decimalMin": "0.00",
        "digits": {
          "integer": 12,
          "fraction": 2
        }
      }
    },
    {
      "name": "status",
      "type": "Enum",
      "enumName": "EmployeeStatus",
      "required": true,
      "filterable": true
    },
    {
      "name": "department",
      "type": "ManyToOne",
      "refEntity": "Department",
      "required": true,
      "filterable": true
    }
  ]
}
```

## Table name dari plugin prefix

`resource create` membaca `plugin.prefix` dari:

```text
plugins/{name}-plugin/src/main/resources/plugin.properties
```

Jika `plugin.prefix` diisi:

```properties
plugin.prefix=auth
```

maka:

```text
Employee -> auth_employees
UserRole -> auth_user_roles
```

Jika `plugin.prefix` kosong:

```properties
plugin.prefix=
```

maka:

```text
Employee -> employees
UserRole -> user_roles
```

### Resource delete

```bash
gasi resource delete Employee
```

Menghapus semua file generated resource untuk entity tertentu dari plugin yang dipilih.

Useful flags:

| Flag                  | Description                                  |
|-----------------------|----------------------------------------------|
| `--include-migration` | Also delete Flyway migration SQL files       |
| `-y, --yes`           | Skips the confirmation prompt                |
| `--cwd <path>`        | Root project, default: current directory     |

## Token Conventions untuk Extend Template

- **Path tokens**: `[[TOKEN]]` — diganti saat copy file/folder
- **Content tokens**: `{{TOKEN}}` — diganti di dalam isi file

Token yang tersedia untuk plugin template:

| Token                    | Contoh value                                  |
|--------------------------|-----------------------------------------------|
| `PLUGIN_NAME`            | `payroll`                                     |
| `PLUGIN_ID`              | `payroll-plugin`                              |
| `PLUGIN_PREFIX`          | `pay` atau empty                              |
| `PLUGIN_VERSION`         | `1.0.0`                                       |
| `PLUGIN_DESCRIPTION`     | `Payroll plugin`                              |
| `PLUGIN_DEPENDENCIES`    | `auth-plugin, ldap-plugin?` atau empty        |
| `PLUGIN_CLASS_NAME`      | `PayrollPlugin`                               |
| `EXTENSION_CLASS_NAME`   | `PayrollAppExtension`                         |
| `FLYWAY_EXT_CLASS_NAME`  | `PayrollFlywayMigrationExtension`             |
| `I18N_EXT_CLASS_NAME`    | `PayrollI18nExtension`                        |
| `DOMAIN`                 | `payroll`                                     |
| `DOMAIN_CAP`             | `Payroll`                                     |
| `BASE_PACKAGE`           | `gasi.gps`                                    |
| `FULL_PACKAGE`           | `gasi.gps.payroll`                            |
| `PKG_PATH`               | `gasi/gps`                                    |
| `FLYWAY_LOCATION`        | `db/migration/payroll`                        |
| `I18N_BASENAME`          | `classpath:i18n/payroll/messages`             |
| `MIGRATION_TIMESTAMP`    | `20260512143052`                              |

Token yang biasanya tersedia untuk resource template tergantung implementasi `resource-generator`, tetapi minimal biasanya mencakup:

| Token / Value      | Contoh value              |
|--------------------|---------------------------|
| `entityName`       | `Employee`                |
| `tableName`        | `auth_employees`          |
| `pluginName`       | `auth`                    |
| `pluginPrefix`     | `auth` atau empty         |
| `pluginPrefixRaw`  | `auth` atau empty         |
| `fields`           | Array field definition    |
```
