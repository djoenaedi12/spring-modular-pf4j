---
name: javadoc-style-guide
description: Javadoc best practices and style guide for writing consistent, proper documentation across the project
---

# Javadoc Style Guide

Panduan ini berlaku untuk semua Java class di project modular Spring Boot PF4J ini. Tujuannya adalah konsistensi dan kejelasan tanpa over-documenting.

## Kapan Wajib Javadoc

| Target | Wajib? | Alasan |
|--------|--------|--------|
| `public` class / interface | ✅ Ya | Entry point bagi developer lain |
| `public` / `protected` method | ✅ Ya | Contract yang harus dipahami |
| Interface method | ✅ Ya | Mendefinisikan kontrak |
| Non-trivial `private` method | ⚠️ Opsional | Kalau logic-nya complex |
| Getter / Setter biasa | ❌ Tidak | Self-explanatory, Lombok generate |
| `@Override` tanpa perubahan behavior | ❌ Tidak | Sudah ada doc di parent |
| `toString()`, `equals()`, `hashCode()` | ❌ Tidak | Standard Java contract |

## Format Dasar

```java
/**
 * Ringkasan singkat dalam satu kalimat (diakhiri titik).
 *
 * <p>Penjelasan tambahan jika diperlukan. Bisa multi-paragraf.
 * Gunakan {@code keyword} untuk literal dan {@link ClassName} untuk referensi.
 *
 * @param paramName deskripsi parameter (huruf kecil, tanpa titik)
 * @param another   deskripsi parameter lain (align jika bisa)
 * @return deskripsi return value
 * @throws BusinessException jika kondisi error terjadi
 * @since 1.0.0
 * @see RelatedClass
 */
```

### Rules

1. **Kalimat pertama** = summary → tampil di Javadoc index, harus singkat dan jelas
2. **Jelaskan KENAPA, bukan APA** → jangan ulangi nama method
3. **`@param`** → wajib untuk setiap parameter
4. **`@return`** → wajib kecuali `void`
5. **`@throws`** → wajib untuk checked exception, opsional untuk unchecked
6. **Gunakan `{@code null}`** → bukan `null` biasa
7. **Gunakan `{@link ClassName#method}`** → untuk cross-reference

## Anti-Patterns

```java
// ❌ BAD — redundant, hanya mengulang nama method
/** Gets the name. */
public String getName() { ... }

// ❌ BAD — terlalu panjang untuk summary
/** This method is used to calculate the total leave balance for an employee
 *  by considering carry-over days from the previous year and subtracting
 *  all approved leave requests. */

// ❌ BAD — tidak ada info berguna
/** Constructor. */
public UserService(UserRepository repo) { ... }

// ✅ GOOD — menjelaskan business intent
/**
 * Calculates remaining leave balance, including carry-over from the previous year.
 *
 * @param employeeId unique identifier of the employee
 * @param year       the fiscal year to calculate
 * @return remaining balance in working days
 * @throws BusinessException if employee is not found
 */
public int calculateLeaveBalance(Long employeeId, int year) { ... }
```

## Per-Layer Guide

### Domain Model

Minimal — cukup satu kalimat deskriptif.

```java
/**
 * Represents an employee leave request within the HR domain.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaveRequest extends BaseModel {

    /** The employee who submitted this request. */
    private User employee;

    /** Start date of the leave period (inclusive). */
    private LocalDate startDate;

    /** End date of the leave period (inclusive). */
    private LocalDate endDate;
}
```

### Service Interface (Inbound Port)

Jelaskan **business contract** — apa yang terjadi, validasi apa yang dilakukan.

```java
/**
 * Use cases for managing employee leave requests.
 */
public interface LeaveService extends BaseService<...> {

    /**
     * Submits a new leave request and notifies the approver.
     *
     * <p>Validates that the employee has sufficient balance
     * and no overlapping requests exist for the same period.
     *
     * @param request the leave request details
     * @return created leave request with generated ID
     * @throws BusinessException if balance is insufficient
     *         or dates overlap with existing requests
     */
    LeaveResponse submitLeave(LeaveCreateRequest request);
}
```

### Service Implementation

Tidak perlu duplikasi doc dari interface. Cukup tambahkan catatan implementasi jika ada.

```java
@Service
public class LeaveServiceImpl extends BaseServiceImpl<...> implements LeaveService {

    // Javadoc sudah di interface — tidak perlu ulang
    @Override
    public LeaveResponse submitLeave(LeaveCreateRequest request) { ... }

    /**
     * Validates all referenced IDs (employee, approver, leave-type)
     * in a single pass and throws one exception with all errors.
     */
    private void validateAllIds(...) { ... }
}
```

### Repository Port (Outbound)

Cukup 1-2 baris. Method dari `BaseRepositoryPort` sudah ter-document.

```java
/**
 * Outbound port for leave request persistence.
 *
 * @see BaseRepositoryPort
 */
public interface LeaveRepositoryPort extends BaseRepositoryPort<LeaveRequest> {
}
```

### REST Controller

Fokus pada endpoint behavior — HTTP method, path, response code.

```java
/**
 * REST API for managing employee leave requests.
 */
@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    /**
     * Submits a new leave request.
     *
     * @param request leave details from the request body
     * @return the created leave request with HTTP 201
     */
    @PostMapping
    public ResponseEntity<LeaveResponse> create(
            @Valid @RequestBody LeaveCreateRequest request) { ... }
}
```

### Configuration Class

Jelaskan **apa yang di-enable** dan kenapa.

```java
/**
 * Enables JPA auditing and provides the current auditor (user ID)
 * for {@code @CreatedBy} and {@code @LastModifiedBy} fields.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig { ... }
```

### Entity (Infrastructure)

Minimal — entity biasanya mirror dari domain model.

```java
/**
 * JPA entity for the {@code leave_requests} table.
 *
 * @see LeaveRequest
 */
@Entity
@Table(name = "leave_requests")
public class LeaveRequestEntity extends BaseEntity { ... }
```

### MapStruct Mapper

Cukup satu baris — MapStruct sudah self-descriptive.

```java
/**
 * Maps between {@link LeaveRequest} domain model and {@link LeaveRequestEntity}.
 */
@Mapper(componentModel = "spring")
public interface LeaveRequestMapper { ... }
```

## Tag Reference

| Tag | Kapan dipakai |
|-----|---------------|
| `@param` | Setiap parameter method |
| `@return` | Setiap non-void return |
| `@throws` | Checked exceptions (wajib), unchecked (opsional) |
| `@see` | Referensi ke class/method terkait |
| `@since` | Fitur baru yang ditambah setelah v1.0 |
| `@deprecated` | Method yang akan dihapus, sertakan `@Deprecated` annotation |
| `{@link Class#method}` | Inline reference ke class/method lain |
| `{@code expression}` | Literal code seperti `{@code null}`, `{@code true}` |
| `{@inheritDoc}` | Inherit doc dari parent, jarang perlu jika `@Override` |

## Referensi

- [Oracle Javadoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [Google Java Style Guide — Section 7](https://google.github.io/styleguide/javaguide.html#s7-javadoc)
