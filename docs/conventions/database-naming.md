# Database & Column Naming Conventions

## General Rules

- Engine: MariaDB (schema = database, satu database untuk semua)
- Migration: Flyway dengan datetime versioning
- Semua nama tabel dan kolom menggunakan **snake_case** lowercase
- Referential integrity dijaga di level database (FK constraint), BUKAN hanya application-level

## Table Naming

### Format

```
{module_prefix}_{entity}    → plural
```

Prefix ditentukan oleh module/plugin yang memiliki tabel tersebut.

Contoh: `core_employees`, `core_departments`, `payroll_payslips`, `hris_leave_requests`

### Rules

- Nama tabel SELALU **plural** → `core_employees` ✅, `core_employee` ❌
- Join/pivot table: `{table1}_{table2}` — **hanya kata terakhir yang plural** → `core_users_roles` ❌, `core_user_roles` ✅
- TIDAK menggunakan shadow table terpisah — shadow record disimpan di tabel utama

## Column Naming

### Naming Convention Summary

| Elemen             | Konvensi                          | Contoh                                  |
|--------------------|-----------------------------------|-----------------------------------------|
| Primary Key        | `id`                              | `id BIGINT PRIMARY KEY`                 |
| Foreign Key        | `{referenced_table_singular}_id`  | `employee_id`, `department_id`          |
| Index              | `idx_{table}_{column(s)}`         | `idx_employees_tenant_status`           |
| Unique Constraint  | `uq_{table}_{column(s)}`         | `uq_employees_emp_no`                   |
| Boolean            | `is_{adjective}` / `has_{noun}`   | `is_active`, `has_mfa_enabled`          |
| Timestamp          | `{action}_at`                     | `created_at`, `approved_at`             |
| Actor              | `{action}_by`                     | `created_by`, `approved_by`             |

### Foreign Key — JANGAN disingkat

```
employee_id     ✅
emp_id          ❌

department_id   ✅
dept_id         ❌
```

## ID Generation Strategy

Menggunakan **sequence**, BUKAN auto increment per tabel. Alasan utama: performance saat insert data.

| Sequence              | Cakupan                  | Keterangan                                                    |
|-----------------------|--------------------------|---------------------------------------------------------------|
| **Global Sequence**   | Seluruh tabel utama      | Satu sequence tunggal untuk semua tabel bisnis                |
| **Audit Log Sequence**| Tabel activity/audit log | Sequence terpisah agar volume audit tidak ganggu tabel bisnis |

Sequence tambahan dapat dibuat jika ada domain yang butuh isolasi serupa.

## Audit Columns (WAJIB di setiap tabel)

Prinsip: setiap perubahan data tercatat — siapa, kapan, dan apa yang berubah. Untuk kepatuhan regulasi ketenagakerjaan dan audit internal.

```sql
created_by    VARCHAR(50)    NOT NULL
created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by    VARCHAR(50)    NULL
updated_at    TIMESTAMP      NULL
```

TIDAK menggunakan soft delete. Jika data perlu dihapus, gunakan hard delete atau pindahkan ke archive table.

## Shadow Record Columns (di tabel utama, untuk approval workflow)

Kolom ini ditambahkan langsung ke tabel utama, BUKAN di tabel terpisah.

```sql
source_id        BIGINT         NULL        -- referensi ke record asli
lifecycle_status VARCHAR(20)    NOT NULL    -- DRAFT, ACTIVE, INACTIVE
```

## Referential Integrity

Semua relasi antar tabel WAJIB dijaga oleh FK constraint di level database. Mengandalkan application-level saja rawan inkonsistensi, terutama jika ada direct query atau migrasi data.

### Rules

1. Semua FK didefinisikan **eksplisit**
2. `ON DELETE`: **RESTRICT** (default) — tidak boleh hapus parent jika masih ada child
3. `ON UPDATE`: **CASCADE** untuk natural key yang mungkin berubah, **RESTRICT** untuk surrogate key

### Contoh

```sql
CONSTRAINT fk_employees_departments
    FOREIGN KEY (department_id)
    REFERENCES departments(id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
```

## Encrypted Columns

- Suffix `_encrypted` → `ktp_number_encrypted`
- Encryption di application-level (AES-256-GCM), BUKAN DB-level

## Enum/Status Columns

- Simpan sebagai `VARCHAR`, BUKAN enum DB
- Enum definition di Java code

## Migration File Naming (Flyway — Datetime Versioning)

```
V{yyyyMMddHHmmss}__{description}.sql
```

Contoh: `V20260309143000__create_employees_table.sql`

Double underscore (`__`) antara version dan description.
