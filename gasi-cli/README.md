# gasi

Developer toolkit untuk modular Spring Boot + PF4J project. Sekarang punya command `plugin`
untuk scaffold, build, deploy, dan clean plugin.

## Install

```bash
cd gasi
npm install
npm link            # registers `gasi` command globally
```

## Usage

### Interactive

```bash
cd /path/to/spring-modular-pf4j
gasi plugin create
```

Akan ditanya: nama plugin, domain, base package, versi, deskripsi, lalu konfirmasi.

### Non-interactive

```bash
gasi plugin create \
  --name payroll \
  --domain payroll \
  --package gasi.gps \
  --plugin-version 1.0.0 \
  --description "Payroll plugin" \
  --yes
```

### Flags

| Flag                   | Deskripsi                                                |
|------------------------|----------------------------------------------------------|
| `-n, --name`           | Nama plugin tanpa suffix `-plugin` (mis. `payroll`)      |
| `-d, --domain`         | Nama Java sub-package (mis. `payroll`)                   |
| `-p, --package`        | Base package (default: `gasi.gps`)                       |
| `-v, --plugin-version` | Versi plugin (default: `1.0.0`)                          |
| `--description`        | Deskripsi plugin                                         |
| `--depends-on`         | Plugin dependency PF4J (lihat di bawah). Bisa diulang.   |
| `--no-flyway`          | Skip Flyway migration sample                             |
| `--no-register`        | Skip auto-register di parent `pom.xml`                   |
| `-y, --yes`            | Pakai semua default tanpa prompt                         |
| `--cwd <path>`         | Root project (default: current directory)                |

## Yang Di-generate

```
{name}-plugin/
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
        │   └── V{timestamp}__init_{name}_schema.sql    (skip jika --no-flyway)
        └── i18n/{name}/
            └── messages.properties
```

Folder Clean Architecture layer (`application/dto`, `domain/model`, `infrastructure/entity`, dst.)
**tidak dibuat di sini** — itu akan dihandle oleh `gasi generate <type>` (atau command serupa, masih TBD) terpisah.

## Plugin Dependencies (`Plugin-Dependencies` di PF4J)

Plugin bisa depend ke plugin lain. Berbeda dari Maven dependency, ini adalah metadata
PF4J yang dipakai untuk classloader & load-order, dan ditulis di manifest JAR.

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
# Single dep
gasi plugin create -n audit ... --depends-on auth-plugin

# Multiple flags
gasi plugin create -n audit ... --depends-on auth-plugin --depends-on 'ldap-plugin?'

# Comma-separated dalam satu flag
gasi plugin create -n audit ... --depends-on 'auth-plugin,ldap-plugin@>=1.0.0?'

# Interactive: CLI scan plugin yang sudah terdaftar di parent pom dan tampilkan multi-select
gasi plugin create
```

Di interactive mode, CLI baca `<modules>` di parent `pom.xml`, ambil yang berakhiran
`-plugin`, dan tawarkan via multi-select. Ada opsi "Tambah manual" untuk masukkan
dependency yang belum di parent pom atau yang butuh version constraint / optional marker.

**Validasi**: jika plugin yang di-depend tidak ada di parent pom, CLI tampilkan warning
tapi tetap lanjut generate. PF4J akan complain saat runtime kalau dependency-nya beneran
tidak ada.

**Output di pom plugin**:
```xml
<plugin.dependencies>auth-plugin, ldap-plugin@>=1.0.0?</plugin.dependencies>
```
Yang kemudian masuk ke `Plugin-Dependencies` di manifest JAR via `${plugin.dependencies}`.

## Auto-register

Jika `--no-register` tidak diset, CLI akan menambahkan `<module>{name}-plugin</module>`
ke blok `<modules>` di parent `pom.xml`. Idempotent: kalau modul sudah ada, di-skip.

## Plugin Lifecycle Commands

### List plugins

```bash
gasi plugin list
```

Lists plugin modules registered in the parent `pom.xml`.

### Build plugin

```bash
gasi plugin build auth
gasi plugin build auth-plugin --skip-tests
gasi plugin build auth --profile dev
```

Runs Maven from the project root:

```bash
mvn clean package -pl auth-plugin -am
```

Useful flags:

| Flag             | Description                                      |
|------------------|--------------------------------------------------|
| `--skip-tests`   | Adds `-DskipTests` to the Maven command          |
| `--profile`      | Adds `-P<name>` to the Maven command             |
| `--dry-run`      | Prints the Maven command without running it      |
| `--verbose`      | Shows full Maven output                          |
| `--cwd <path>`   | Root project (default: current directory)        |

### Deploy plugin

```bash
gasi plugin deploy auth
gasi plugin deploy auth --plugins-dir platform-app/plugins
gasi plugin deploy auth --keep-old
```

Finds the main JAR under `{plugin}/target/` and copies it to
`platform-app/plugins/`. It skips `sources`, `javadoc`, and `original-*` JARs.

Useful flags:

| Flag                   | Description                                         |
|------------------------|-----------------------------------------------------|
| `--plugins-dir <path>` | Plugin deployment directory                         |
| `--keep-old`           | Keep older deployed JARs for the same plugin        |
| `--dry-run`            | Prints deploy actions without changing files        |
| `--cwd <path>`         | Root project (default: current directory)           |

### Clean deployed plugin

```bash
gasi plugin clean auth
gasi plugin clean auth --plugins-dir platform-app/plugins
```

Removes deployed JARs for the selected plugin from the plugins directory.
It does not delete the plugin source module or its `target/` folder.

### Delete plugin

```bash
gasi plugin delete payroll
gasi plugin delete payroll --dry-run
gasi plugin delete payroll --yes
gasi plugin remove payroll
```

Deletes a plugin source module, removes its `<module>` entry from the parent
`pom.xml`, and removes deployed JARs for that plugin from `platform-app/plugins/`.
This command asks for confirmation unless `--yes` is provided.

Useful flags:

| Flag                   | Description                                         |
|------------------------|-----------------------------------------------------|
| `--plugins-dir <path>` | Plugin deployment directory                         |
| `--keep-deployed`      | Keep deployed JARs in the plugins directory         |
| `--dry-run`            | Prints delete actions without changing files        |
| `-y, --yes`            | Skips the confirmation prompt                       |
| `--cwd <path>`         | Root project (default: current directory)           |

## Token Conventions (untuk yang mau extend template)

- **Path tokens**: `[[TOKEN]]` — diganti saat copy file/folder
- **Content tokens**: `{{TOKEN}}` — diganti di dalam isi file

Token yang tersedia:

| Token                    | Contoh value                                  |
|--------------------------|-----------------------------------------------|
| `PLUGIN_NAME`            | `payroll`                                     |
| `PLUGIN_ID`              | `payroll-plugin`                              |
| `PLUGIN_VERSION`         | `1.0.0`                                       |
| `PLUGIN_DESCRIPTION`     | `Payroll plugin`                   |
| `PLUGIN_DEPENDENCIES`    | `auth-plugin, ldap-plugin?` (comma-separated, atau empty) |
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
| `MIGRATION_TIMESTAMP`    | `20260512143052` (YYYYMMDDHHmmss saat generate) |
