# JavaDoc Conventions

Panduan ini berlaku untuk semua Java class di project modular Spring Boot PF4J ini. Tujuannya adalah konsistensi dan kejelasan tanpa over-documenting. Semua JavaDoc ditulis dalam **Bahasa Inggris**.

## Kapan Wajib JavaDoc

| Target | Wajib? | Alasan |
|--------|--------|--------|
| `public` class / interface | ✅ Ya | Entry point bagi developer lain |
| `public` / `protected` method | ✅ Ya | Contract yang harus dipahami |
| Interface method | ✅ Ya | Mendefinisikan kontrak |
| DTO field (non-obvious) | ✅ Ya | Field dengan business logic |
| Non-trivial `private` method | ⚠️ Opsional | Jika logic > 10 baris |
| Getter / Setter biasa | ❌ Tidak | Self-explanatory, Lombok generate |
| `@Override` tanpa perubahan behavior | ❌ Tidak | Sudah ada doc di parent |
| `toString()`, `equals()`, `hashCode()` | ❌ Tidak | Standard Java contract |

## Format

### Class / Interface

```java
/**
 * Manages CRUD operations for employee data.
 *
 * <p>This port implementation handles business validation,
 * ID encoding via Sqids, and audit logging.</p>
 *
 * @since 1.0.0
 * @see EmployeeRepositoryPort
 */
public class EmployeeService implements CreateEmployeeUseCase {
```

### Method

```java
/**
 * Calculates remaining leave balance, including carry-over from the previous year.
 *
 * <p>Validates that the employee exists and has an active contract
 * before performing the calculation.</p>
 *
 * @param employeeId unique identifier of the employee
 * @param year       the fiscal year to calculate
 * @return remaining balance in working days
 * @throws ResourceNotFoundException if employee is not found
 * @throws BusinessException if contract is inactive
 */
public int calculateLeaveBalance(Long employeeId, int year) {
```

### Enum

```java
/**
 * Lifecycle status of a record in the system.
 *
 * <p>Used with the shadow record pattern for approval workflows.</p>
 */
public enum LifecycleStatus {
    /** Newly created, not yet active. */
    DRAFT,
    /** Active and in effect. */
    ACTIVE,
    /** Deactivated. */
    INACTIVE
}
```

## Rules

1. **First sentence** = summary → appears in Javadoc index, must be concise and standalone
2. **Explain WHY, not WHAT** → don't repeat the method name
3. **`@param`** → required for every parameter
4. **`@return`** → required unless `void`
5. **`@throws`** → required for checked exceptions, optional for important unchecked
6. **`@see`** → use for cross-reference to related class/method
7. **`@since`** → for features added after v1.0
8. **Use `{@code null}`** → not plain `null`
9. **Use `{@link ClassName#method}`** → for inline reference to other class/method
10. **Keep JavaDoc up to date** → update when method behavior changes
11. **Service implementation** → don't duplicate doc from interface, only add implementation-specific notes if needed

## Anti-Patterns

```java
// ❌ BAD — redundant, just repeats method name
/** Gets the name. */
public String getName() { ... }

// ❌ BAD — too long for summary line
/** This method is used to calculate the total leave balance for an employee
 *  by considering carry-over days from the previous year and subtracting
 *  all approved leave requests. */

// ❌ BAD — no useful information
/** Constructor. */
public UserService(UserRepository repo) { ... }

// ✅ GOOD — explains business intent
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

## Tag Reference

| Tag | Kapan dipakai |
|-----|---------------|
| `@param` | Setiap parameter method |
| `@return` | Setiap non-void return |
| `@throws` | Checked exceptions (wajib), unchecked yang penting (opsional) |
| `@see` | Referensi ke class/method terkait |
| `@since` | Fitur baru yang ditambah setelah v1.0 |
| `@deprecated` | Method yang akan dihapus, sertakan `@Deprecated` annotation |
| `{@link Class#method}` | Inline reference ke class/method lain |
| `{@code expression}` | Literal code seperti `{@code null}`, `{@code true}` |
| `{@inheritDoc}` | Inherit doc dari parent, jarang perlu jika `@Override` |

## Referensi

- [Oracle Javadoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [Google Java Style Guide — Section 7](https://google.github.io/styleguide/javaguide.html#s7-javadoc)
