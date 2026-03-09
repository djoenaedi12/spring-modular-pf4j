# Unit Test Conventions

## Framework & Tools

- JUnit 5 (Jupiter)
- Mockito untuk mocking
- AssertJ untuk fluent assertions (BUKAN JUnit assertions)
- `@ExtendWith(MockitoExtension.class)` — JANGAN pakai `@SpringBootTest` untuk unit test

## Test Class Naming

```
{ClassUnderTest}Test
```
Contoh: `EmployeeServiceTest`, `EmployeeDtoMapperTest`

Lokasi: `src/test/java/` — mirror package structure dari main source.

## Test Method Naming

### Format
```
should_{expectedBehavior}_when_{condition}
```

### Contoh
```java
@Test
void should_createEmployee_when_validRequest() { }

@Test
void should_throwDuplicateException_when_nikAlreadyExists() { }

@Test
void should_returnEncodedId_when_employeeFound() { }

@Test
void should_throwUnauthorized_when_userHasNoAccess() { }
```

### JANGAN
```java
@Test
void test1() { }                    // ❌ tidak deskriptif
@Test
void testCreateEmployee() { }       // ❌ tidak jelas kapan/apa
@Test
void createEmployeeTest() { }       // ❌ suffix Test bukan di method
```

## Test Structure — AAA Pattern

SETIAP test method WAJIB mengikuti Arrange-Act-Assert:

```java
@Test
void should_createEmployee_when_validRequest() {
    // Arrange
    var request = EmployeeCreateRequest.builder()
            .nik("12345")
            .fullName("John Doe")
            .build();

    when(employeePort.existsByNik("12345")).thenReturn(false);
    when(employeePort.save(any())).thenReturn(mockEmployee);
    when(idEncoder.encode(1L)).thenReturn("abc123");

    // Act
    var result = employeeService.createEmployee(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo("abc123");
    assertThat(result.getFullName()).isEqualTo("John Doe");

    verify(employeePort).save(any());
    verify(auditContext).log(any());
}
```

## Apa yang WAJIB Ditest

### Service Layer (prioritas utama)
- ✅ Happy path — input valid, return expected
- ✅ Validation failure — input invalid, throw exception
- ✅ Authorization — user tanpa akses, throw UnauthorizedException
- ✅ Edge case — null, empty string, boundary values
- ✅ ID encoding — pastikan Sqids encode dipanggil, bukan raw ID yang di-return
- ✅ Audit logging — verify audit context dipanggil

### Mapper Layer
- ✅ DTO → Entity mapping (semua field)
- ✅ Entity → DTO mapping (termasuk ID encoding)
- ✅ `@IgnoreAuditFields` dan `@IgnoreSecurityFields` benar-benar di-ignore
- ✅ Null handling

### Controller Layer (opsional, cukup integration test)
- Fokus di status code dan response structure
- Gunakan `@WebMvcTest` bukan `@SpringBootTest`

## Apa yang TIDAK Perlu Ditest

- ❌ Getter/Setter (kecuali ada logic)
- ❌ Spring configuration class
- ❌ Framework behavior (Flyway migration, Spring Security filter chain)
- ❌ Third-party library internal

## Mocking Rules

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepositoryPort employeePort;

    @Mock
    private IdEncoder idEncoder;

    @Mock
    private AuditContext auditContext;

    @InjectMocks
    private EmployeeService employeeService;
}
```

1. Mock PORT interface, BUKAN concrete implementation
2. Gunakan `@InjectMocks` untuk class under test
3. JANGAN mock value object atau DTO — buat instance asli
4. Gunakan `verify()` untuk memastikan side effect terjadi (save, audit log, dsb)
5. Gunakan `verifyNoMoreInteractions()` jika perlu memastikan tidak ada call tak terduga

## Assertions — AssertJ Style

```java
// ✅ AssertJ (preferred)
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("John");
assertThat(list).hasSize(3).extracting("name").contains("John", "Jane");
assertThatThrownBy(() -> service.delete(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");

// ❌ JUnit assertions (jangan pakai)
assertNotNull(result);
assertEquals("John", result.getName());
```

## Test Data

- Gunakan Builder pattern atau factory method untuk test data
- JANGAN hardcode magic number tanpa penjelasan
- Buat helper class `TestDataFactory` jika test data dipakai berulang:

```java
class TestDataFactory {
    static EmployeeCreateRequest validEmployeeRequest() {
        return EmployeeCreateRequest.builder()
                .nik("12345")
                .fullName("John Doe")
                .departmentId("encoded-dept-id")
                .build();
    }
}
```

## Coverage Target

- Service layer: minimal 80%
- Mapper layer: minimal 90%
- Overall project: minimal 70%
- Fokus di meaningful coverage, bukan angka — test behavior, bukan line count
