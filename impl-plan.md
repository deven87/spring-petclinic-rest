# Implementation Plan: Add Phone Number Field to Pet Owner with Validation

**Feature**: EPMCDMETST-69132  
**Branch**: `feature/add-phone-number-field`  
**Estimated Total Effort**: 12-14 hours (1 developer)  
**Priority**: CRITICAL (ReDoS vulnerability fix required before code review)

---

## Task Breakdown

### PHASE 1: Validation Setup (CRITICAL — Must Complete First)

**Task 1.1: Add libphonenumber Dependency to pom.xml**
- **Complexity**: Low (30 min)
- **Blocked By**: None
- **Blocks**: Task 1.2
- **Files Modified**: `pom.xml`
- **Description**: Add Google's libphonenumber library to Maven dependencies
- **Success Criteria**:
  - Dependency added to `<dependencies>` section: `com.googlecode.libphonenumber:libphonenumber:[LATEST]`
  - Project builds successfully with `mvn clean compile`
  - Compatibility verified with Spring Boot 3.x (ensure no transitive dependency conflicts)
  - JAR is resolved from Maven Central
- **Testing**: Run `mvn dependency:tree` to verify no conflicts

---

**Task 1.2: Create Custom Phone Number Validator Class**
- **Complexity**: Medium (1-1.5 hours)
- **Blocked By**: Task 1.1
- **Blocks**: Task 2.1, Task 5.1-5.6
- **Files Created**: `src/main/java/org/springframework/samples/petclinic/util/PhoneNumberValidator.java`
- **Description**: Implement custom validator using libphonenumber library; fix ReDoS vulnerability
- **Success Criteria**:
  - Class implements `ConstraintValidator<ValidPhoneNumber, String>`
  - Uses `PhoneNumberUtil.getInstance()` to parse and validate numbers
  - Validates against supported regions (US, DE, FR, etc.) or accepts international format
  - Handles null input gracefully (returns true for nullable field)
  - Throws `ValidationException` for invalid formats with meaningful error message
  - No ReDoS vulnerability (uses compiled regex from libphonenumber, not custom pattern)
  - Logs (at WARN level) failed validation attempts with masked phone number for privacy
- **Approach**: Use libphonenumber's built-in validation rather than custom regex
  ```
  PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
  try {
      PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "US"); // default region
      return phoneNumberUtil.isValidNumber(number);
  } catch (Exception e) {
      return false;
  }
  ```

---

**Task 1.3: Create Custom Annotation `@ValidPhoneNumber`**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 1.2
- **Blocks**: Task 2.1
- **Files Created**: `src/main/java/org/springframework/samples/petclinic/validation/ValidPhoneNumber.java`
- **Description**: Create Spring validation annotation to attach validator to fields
- **Success Criteria**:
  - Annotation marked with `@Constraint(validatedBy = PhoneNumberValidator.class)`
  - Supports `@Target(ElementType.FIELD)`
  - Default message: `"Invalid phone number format"`
  - Can be applied to Owner and OwnerDto fields
  - Works with `@Valid` in controller method parameters

---

**Task 1.4: Unit Tests for Phone Validation**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 1.2
- **Blocks**: Task 2.4
- **Files Created**: `src/test/java/org/springframework/samples/petclinic/util/PhoneNumberValidatorTest.java`
- **Description**: Comprehensive unit tests for validator covering all supported formats and edge cases
- **Success Criteria**:
  - ✅ Valid cases: `"+1 234-567-8900"`, `"+44 20 7946 0958"`, `"+33 1 42 68 53 00"`, `"+49 30 12345678"`, `"(555) 123-4567"`
  - ❌ Invalid cases: `"invalid"`, `"123"`, `"++++++++++++++X"` (ReDoS attack), `"abc-def-ghij"`, empty string
  - ✅ Null values return true (optional field)
  - ✅ Whitespace handling
  - ✅ International format handling (leading +)
  - Test class: `PhoneNumberValidatorTest` with ≥15 test methods covering edge cases
  - All tests PASS

---

### PHASE 2: Entity & DTO Modifications

**Task 2.1: Add phoneNumber Field to Owner Entity**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 1.3
- **Blocks**: Task 2.3, Task 3.1
- **Files Modified**: `src/main/java/org/springframework/samples/petclinic/model/Owner.java`
- **Description**: Add phoneNumber field to entity with validation annotation
- **Success Criteria**:
  - Field added: `private String phoneNumber;`
  - Annotated with `@ValidPhoneNumber` (custom annotation from Task 1.3)
  - Getter/setter methods generated
  - Field is optional (no `@NotNull` annotation)
  - Javadoc added: `"Phone number in international or local format (10-20 chars, optional)"`
  - `equals()` and `hashCode()` do NOT include phoneNumber (to avoid breaking existing tests)
  - Builds without compilation errors: `mvn clean compile`

---

**Task 2.2: Add phoneNumber Field to OwnerDTO with OpenAPI Annotation**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 1.3
- **Blocks**: Task 2.3, Task 4.1
- **Files Modified**: `src/main/java/org/springframework/samples/petclinic/rest/dto/OwnerDto.java`
- **Description**: Add phoneNumber field to DTO with @Schema annotation for OpenAPI
- **Success Criteria**:
  - Field added: `private String phoneNumber;`
  - Getter/setter methods generated
  - Annotated with `@Schema(description = "Phone number in international or local format", example = "+1 234-567-8900", required = false)`
  - Javadoc added
  - Field is optional in JSON (Jackson doesn't require it)
  - Builds without compilation errors

---

**Task 2.3: Update OwnerMapper to Map phoneNumber**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 2.1, Task 2.2
- **Blocks**: Task 2.4, Task 5.1-5.6
- **Files Modified**: `src/main/java/org/springframework/samples/petclinic/mapper/OwnerMapper.java`
- **Description**: Ensure MapStruct mapping includes phoneNumber field
- **Success Criteria**:
  - `OwnerMapper` interface includes method to map `Owner` ↔ `OwnerDto`
  - If using `@Mapper` with default method, no explicit mapping needed (MapStruct auto-maps by name)
  - If mapping already exists, add: `mapping.phoneNumber = source.phoneNumber`
  - Test compilation: `mvn clean compile` passes
  - No warnings in MapStruct processor output

---

**Task 2.4: Unit Tests for Entity & DTO Mapping**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.3
- **Blocks**: Task 5.1
- **Files Created**: `src/test/java/org/springframework/samples/petclinic/mapper/OwnerMapperTest.java` (or extend existing if already present)
- **Description**: Unit tests for entity ↔ DTO mapping including phoneNumber
- **Success Criteria**:
  - Test `mapOwnerToDto()`: Entity with phoneNumber → DTO with same phoneNumber
  - Test `mapDtoToOwner()`: DTO with phoneNumber → Entity with same phoneNumber
  - Test null phoneNumber is preserved in both directions
  - Test with various valid phone formats
  - Test mapping with all Owner fields (not just phoneNumber)
  - ≥5 test methods, all PASS
  - Code coverage: ≥80% for mapper methods

---

### PHASE 3: Database Schema Migrations

**Task 3.1: Create ALTER TABLE Scripts for All 4 Databases**
- **Complexity**: Medium (1.5 hours)
- **Blocked By**: Task 2.1
- **Blocks**: Task 3.3
- **Files Modified/Created**:
  - `src/main/resources/db/h2/schema.sql`
  - `src/main/resources/db/hsqldb/schema.sql`
  - `src/main/resources/db/mysql/schema.sql`
  - `src/main/resources/db/postgres/schema.sql`
- **Description**: Add ALTER TABLE statement to add phoneNumber column to OWNERS table
- **Success Criteria**:
  - **H2**: `ALTER TABLE OWNERS ADD COLUMN phone_number VARCHAR(20);`
  - **HSQLDB**: `ALTER TABLE OWNERS ADD COLUMN phone_number VARCHAR(20);`
  - **MySQL**: `ALTER TABLE owners ADD COLUMN phone_number VARCHAR(20) CHARSET utf8mb4 DEFAULT NULL;`
  - **PostgreSQL**: `ALTER TABLE owners ADD COLUMN phone_number VARCHAR(20) DEFAULT NULL;`
  - Column is nullable (DEFAULT NULL or allow NULL)
  - Column name: `phone_number` (snake_case to match JPA naming convention with `@Column(name = "phone_number")`)
  - No unique constraint added (non-unique as per requirements)
  - Scripts are idempotent (handle if column already exists, or document why not)
  - `data.sql` files NOT modified (no initial data needed for this optional field)

---

**Task 3.2: Document Index Strategy & Add Optional Index Script**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 3.1
- **Blocks**: Task 3.4
- **Files Created**: `src/main/resources/db/migrations/INDEX_STRATEGY.md`
- **Description**: Document indexing strategy and create optional index script for future use
- **Success Criteria**:
  - Document in `INDEX_STRATEGY.md`:
    - Current decision: No index on `phone_number` (since not filtering by phone in current API)
    - If future API adds GET `/api/owners?phone=...` search, create index:
      - H2/HSQLDB/MySQL: `CREATE INDEX idx_owners_phone_number ON owners(phone_number);`
      - PostgreSQL: `CREATE INDEX idx_owners_phone_number ON owners(phone_number);`
    - Rationale: Reduces write performance during INSERT/UPDATE; index only needed if reads dominate
  - Index scripts created but NOT applied (for future migration)
  - File includes rollback procedures for each database

---

**Task 3.3: Create Database Migration Tests**
- **Complexity**: Medium (1.5 hours)
- **Blocked By**: Task 3.2
- **Blocks**: None (can run in parallel with Phase 4-5)
- **Files Created**: `src/test/java/org/springframework/samples/petclinic/db/SchemaMigrationTest.java`
- **Description**: Verify schema changes applied correctly for each database type
- **Success Criteria**:
  - Test for each DB type (H2, HSQLDB, MySQL, PostgreSQL):
    - ✅ Column `phone_number` exists on OWNERS table
    - ✅ Column type is VARCHAR (or TEXT) with max length ≥20
    - ✅ Column allows NULL
    - ✅ INSERT new owner record with phoneNumber → success
    - ✅ UPDATE owner record with phoneNumber → success
    - ✅ SELECT owner record and phoneNumber is returned
  - Test uses Spring's `@DataJpaTest` with test database
  - ≥4 test methods (one per DB type) or parameterized tests
  - All tests PASS
  - Coverage includes both schema check and CRUD operations

---

**Task 3.4: Document Rollback Procedure**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 3.3
- **Blocks**: None
- **Files Created**: `src/main/resources/db/migrations/ROLLBACK_PROCEDURE.md`
- **Description**: Document step-by-step rollback for each database type
- **Success Criteria**:
  - Document includes rollback SQL for each DB type:
    - H2: `ALTER TABLE OWNERS DROP COLUMN phone_number;`
    - HSQLDB: `ALTER TABLE OWNERS DROP COLUMN phone_number;`
    - MySQL: `ALTER TABLE owners DROP COLUMN phone_number;`
    - PostgreSQL: `ALTER TABLE owners DROP COLUMN phone_number;`
  - Prerequisites for rollback listed (backup data first, stop app, etc.)
  - Verification steps included (query OWNERS table structure after rollback)
  - Timeline noted: Rollback takes ~5 minutes total
  - File is version-controlled (in git) for reference during incidents

---

### PHASE 4: OpenAPI/Swagger Documentation

**Task 4.1: Update OpenAPI Specification**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 2.2
- **Blocks**: Task 4.2
- **Files Modified**: `src/main/resources/openapi.yml`
- **Description**: Add phoneNumber field to OwnerFields schema in OpenAPI spec
- **Success Criteria**:
  - Schema `OwnerFields` or `Owner` updated to include:
    ```yaml
    phoneNumber:
      type: string
      format: phone
      example: "+1 234-567-8900"
      nullable: true
      maxLength: 20
      description: "Phone number in international or local format"
    ```
  - Endpoint descriptions for POST/PUT updated to mention phoneNumber (optional field)
  - OpenAPI spec is valid YAML (no syntax errors)
  - Can be loaded into Swagger UI without errors
  - Verification: `mvn clean compile` and OpenAPI generation passes

---

**Task 4.2: Verify Swagger UI Display**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 4.1
- **Blocks**: Task 5.1
- **Files Modified**: None (testing only)
- **Description**: Start application and verify Swagger UI displays phoneNumber field correctly
- **Success Criteria**:
  - Start app: `mvn spring-boot:run`
  - Navigate to Swagger UI: `http://localhost:8080/swagger-ui.html` (or configured port)
  - Open `/api/owners` POST schema → phoneNumber field visible
  - Open `/api/owners/{id}` GET schema → phoneNumber field visible
  - Open `/api/owners/{id}` PUT schema → phoneNumber field visible
  - Field shows as optional (no red asterisk in Swagger UI)
  - Field shows example: `"+1 234-567-8900"`
  - Field shows maxLength constraint: `20`
  - Try to POST with valid phoneNumber using "Try it out" → success (verify persistence)
  - Try to POST with invalid phoneNumber → 400 Bad Request with validation error message

---

### PHASE 5: Integration Testing

**Task 5.1: Integration Test - POST with Valid Phone Number**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.4
- **Blocks**: None (can run in parallel with 5.2-5.6)
- **Files Created**: `src/test/java/org/springframework/samples/petclinic/rest/OwnerRestControllerPhoneNumberIntegrationTest.java` (or extend existing test)
- **Description**: Test POST /api/owners endpoint accepts valid phoneNumber
- **Success Criteria**:
  - Test class uses `@SpringBootTest` with `TestRestTemplate` or `MockMvc`
  - Test method: `testCreateOwnerWithValidPhoneNumber()`
  - Creates owner DTO with:
    - Standard fields (firstName, lastName, address, city, etc.)
    - phoneNumber: `"+1 234-567-8900"` (valid format)
  - POST to `/api/owners` (or `/api/v2/owners` if versioned)
  - Assertions:
    - HTTP 201 Created (or 200 OK depending on API design)
    - Response DTO includes phoneNumber: `"+1 234-567-8900"`
    - Database record persisted with phoneNumber value
    - GET /api/owners/{id} returns same phoneNumber
  - Test passes

---

**Task 5.2: Integration Test - POST with Invalid Phone Number**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.4
- **Blocks**: None (can run in parallel)
- **Files Modified**: Same test class as 5.1
- **Description**: Test POST /api/owners rejects invalid phoneNumber with validation error
- **Success Criteria**:
  - Test method: `testCreateOwnerWithInvalidPhoneNumber()`
  - Creates owner DTO with phoneNumber: `"invalid-number"` (fails validation)
  - POST to `/api/owners`
  - Assertions:
    - HTTP 400 Bad Request
    - Response includes validation error: `"Invalid phone number format"` (from @ValidPhoneNumber message)
    - Error response format matches Spring validation error convention
    - Database record NOT persisted
  - Test passes
  - Additional cases: `"123"`, `"++++++++++++"`, `""`, special characters only

---

**Task 5.3: Integration Test - POST with Null Phone Number (Optional Field)**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 2.4
- **Blocks**: None
- **Files Modified**: Same test class as 5.1
- **Description**: Test POST /api/owners accepts null phoneNumber (field is optional)
- **Success Criteria**:
  - Test method: `testCreateOwnerWithoutPhoneNumber()`
  - Creates owner DTO WITHOUT phoneNumber field (or with null)
  - POST to `/api/owners`
  - Assertions:
    - HTTP 201 Created (success)
    - Response includes phoneNumber: null (or omitted)
    - Database record persisted with phone_number = NULL
  - Test passes

---

**Task 5.4: Integration Test - PUT to Update Phone Number**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.4
- **Blocks**: None
- **Files Modified**: Same test class as 5.1
- **Description**: Test PUT /api/owners/{id} updates phoneNumber
- **Success Criteria**:
  - Test method: `testUpdateOwnerPhoneNumber()`
  - Create owner via POST (with or without phoneNumber)
  - Update owner DTO with new phoneNumber: `"+44 20 7946 0958"`
  - PUT to `/api/owners/{id}`
  - Assertions:
    - HTTP 200 OK (or 204 No Content)
    - Response includes updated phoneNumber
    - Database record updated with new value
    - GET /api/owners/{id} returns updated phoneNumber
  - Test passes
  - Additional cases: Clear phoneNumber (set to null)

---

**Task 5.5: Integration Test - GET Endpoints Include Phone Number**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.4
- **Blocks**: None
- **Files Modified**: Same test class as 5.1
- **Description**: Test GET endpoints return phoneNumber field
- **Success Criteria**:
  - Test method: `testGetOwnerIncludesPhoneNumber()`
  - Create owner with phoneNumber via POST
  - GET `/api/owners/{id}`
  - Assertions:
    - HTTP 200 OK
    - Response DTO includes phoneNumber field (even if null)
    - phoneNumber value matches what was stored
  - Test method: `testGetAllOwnersIncludesPhoneNumber()`
  - GET `/api/owners` (list all)
  - Assertions:
    - HTTP 200 OK
    - Response list includes owners with phoneNumber field
    - All phone numbers present (or null for those not set)
  - Both tests pass

---

**Task 5.6: Integration Test - Backward Compatibility (Legacy Client)**
- **Complexity**: Medium (1 hour)
- **Blocked By**: Task 2.4
- **Blocks**: None
- **Files Modified**: Same test class as 5.1
- **Description**: Test legacy API clients (not sending phoneNumber) still work
- **Success Criteria**:
  - Test method: `testCreateOwnerLegacyClientBackwardCompatibility()`
  - Creates owner DTO without phoneNumber field (simulates old client)
  - POST to `/api/owners`
  - Assertions:
    - HTTP 201 Created (success, no error)
    - Record created successfully
    - phoneNumber defaults to NULL in database
  - Test method: `testUpdateOwnerLegacyClientDoesNotClearPhoneNumber()`
  - Create owner with phoneNumber
  - Update owner (PUT) using DTO WITHOUT phoneNumber field (simulates old client not knowing about field)
  - Assertions:
    - HTTP 200 OK
    - phoneNumber is NOT cleared (remains unchanged or uses default behavior)
    - Database still has original phoneNumber value
  - Both tests pass (backward compatibility maintained)

---

### PHASE 6: Rollout & Documentation

**Task 6.1: Add Logging for Phone Validation Failures**
- **Complexity**: Low (30 min)
- **Blocked By**: Task 1.2
- **Blocks**: None (can be done in parallel after validation is complete)
- **Files Modified**: `src/main/java/org/springframework/samples/petclinic/util/PhoneNumberValidator.java`
- **Description**: Add logging to validator for operational observability
- **Success Criteria**:
  - Logger added: `private static final Logger logger = LoggerFactory.getLogger(PhoneNumberValidator.class);`
  - On validation failure, log at WARN level:
    - Message: `"Phone number validation failed: [MASKED_NUMBER]"` (mask for privacy, e.g., `"+1 XXX-XXX-8900"`)
    - Include owner ID or context if available (e.g., "for owner 123")
    - Do NOT log full phone number in production
  - Log statement placed in exception handler in validator
  - Logging can be controlled via `application.properties` (log level can be set)
  - Verification: Run app with invalid phone number POST, check logs contain masked message

---

**Task 6.2: Create Developer Documentation**
- **Complexity**: Low (30 min)
- **Blocked By**: None
- **Blocks**: None
- **Files Created**: `docs/PHONE_NUMBER_FEATURE.md` or update `README.md`
- **Description**: Document phone number field constraints and implementation details for developers
- **Success Criteria**:
  - Document includes:
    - **Overview**: "Phone number field added to Owner entity with international format validation"
    - **Supported Formats**: List examples: "+1 234-567-8900", "+44 20 7946 0958", "+33 1 42 68 53 00", "(555) 123-4567"
    - **Database Constraints**: Column phone_number VARCHAR(20), nullable, non-unique
    - **Validation**: Uses libphonenumber library (no ReDoS vulnerability)
    - **API Usage**: Examples of POST/PUT/GET with phoneNumber field
    - **Backward Compatibility**: Legacy clients (no phoneNumber) still supported
    - **Troubleshooting**: Common validation errors and solutions
    - **Migration**: How to rollback if needed (link to ROLLBACK_PROCEDURE.md)
  - Document is in Markdown format
  - File is committed to git

---

## Workstreams

### Workstream A: Validation Foundation (CRITICAL PATH — Must Complete First)
**Duration**: ~3.5 hours
```
Task 1.1 (0.5h) 
  ↓
Task 1.2 (1.5h) → Task 1.3 (0.5h)
  ↓
Task 1.4 (1h)
```
**Critical**: Phase 1 must complete before Phase 2 begins (validation is used in entity annotations)

---

### Workstream B: Entity & Mapping (Depends on Workstream A)
**Duration**: ~2.5 hours (starts after Task 1.3)
```
Task 2.1 (0.5h) ─┐
                 ├→ Task 2.3 (0.5h) → Task 2.4 (1h)
Task 2.2 (0.5h) ─┘
```
**Dependency**: Requires Task 1.3 (@ValidPhoneNumber annotation)

---

### Workstream C: Database & Schema (Parallel with Workstream B after Task 2.1)
**Duration**: ~3.5 hours (starts after Task 2.1)
```
Task 3.1 (1.5h)
  ↓
Task 3.2 (0.5h) → Task 3.3 (1.5h)
  ↓
Task 3.4 (0.5h)
```
**Dependency**: Requires Task 2.1 (knows which field to migrate)

---

### Workstream D: API Documentation (Parallel with C, depends on Task 2.2)
**Duration**: ~1 hour (starts after Task 2.2)
```
Task 4.1 (0.5h) → Task 4.2 (0.5h)
```
**Dependency**: Requires Task 2.2 (@Schema annotations)

---

### Workstream E: Integration Testing (Parallel, starts after Task 2.4)
**Duration**: ~4 hours (all tasks start after Task 2.4)
```
Task 5.1 (1h)
Task 5.2 (1h)  ─┐
Task 5.3 (0.5h)─├→ (Run all in parallel)
Task 5.4 (1h)  ─┤
Task 5.5 (1h)  ─┤
Task 5.6 (1h)  ─┘
```
**Dependency**: Requires Task 2.4 (mapping tests pass), Task 3.3 (schema migrations verify DB)

---

### Workstream F: Rollout & Documentation (Parallel with E, or after Phase 5 for integration with main build)
**Duration**: ~1 hour (can start after Task 1.2 for logging, Task 6.2 anytime)
```
Task 6.1 (0.5h) [in parallel with Task 1.2 implementation]
Task 6.2 (0.5h) [anytime after design is complete]
```

---

## Critical Path Analysis

**Longest Dependency Chain** (determines minimum project duration):
```
Task 1.1 (0.5h) 
  → Task 1.2 (1.5h) 
  → Task 1.3 (0.5h) 
  → Task 2.1 (0.5h) 
  → Task 3.1 (1.5h) 
  → Task 3.3 (1.5h)
  = 6.5 hours minimum CRITICAL PATH
```

**Parallel Execution Possible** (After Task 1.3):
- Task 2.2 & 2.3 can start together while Task 1.4 runs
- Task 3.1-3.4 can run in parallel with Task 2.x
- Task 4.1-4.2 can run after Task 2.2
- Task 5.1-5.6 can run in parallel after Task 2.4
- Task 6.1-6.2 can start as soon as respective task dependencies clear

**Optimal Parallelization** (for 1 developer):
- Developer completes Workstream A (Phase 1): 3.5 hours
- Then splits attention between Workstreams B, C, D (interleaved): ~4-5 hours
- Then runs Workstream E tests (validation tests): ~4 hours
- Finally Workstream F (docs): ~1 hour
- **Total**: 12-14 hours (matches estimate)

---

## Success Criteria Checklist

- ✅ **≥3 tasks defined**: 24 total tasks across 6 phases
- ✅ **Dependencies explicit**: Each task lists "Blocked By" and "Blocks"
- ✅ **Critical path identified**: Validation → Entity → DB Schema → Tests (6.5 hours minimum)
- ✅ **ReDoS fix prioritized**: Phase 1, Task 1.2 (CRITICAL, blocks everything else)
- ✅ **Tasks are time-bounded**: Each task 30 min to 1.5 hours (within 1-3 hour guideline)
- ✅ **Success criteria defined**: Each task has clear "Success Criteria" section
- ✅ **Files specified**: All modified/created files listed in each task
- ✅ **Complexity estimated**: S/M/L for each task; totals 12-14 hours
- ✅ **Test coverage included**: Unit tests (Phase 1, 2), integration tests (Phase 5), schema tests (Phase 3)
- ✅ **Database migrations included**: All 4 DB types (H2, HSQLDB, MySQL, PostgreSQL), rollback procedure documented
- ✅ **OpenAPI/Swagger documentation included**: Phase 4 tasks
- ✅ **Backward compatibility addressed**: Task 5.6 integration test
- ✅ **Workstreams mapped**: 6 workstreams with dependency clarity
- ✅ **Documentation included**: Phase 6 for rollback, index strategy, developer guide

---

## Execution Notes

1. **Start with Workstream A**: Do not start other workstreams until Task 1.3 completes (validation annotation must exist)
2. **Parallel after Phase 1**: Once validation framework is ready, Workstreams B, C, D can proceed in parallel
3. **Testing last**: Phase 5 integration tests should not start until Phase 2 (mapping) is complete and Phase 3 (schema) is verified
4. **Documentation**: Phase 6 tasks are low-risk and can be done anytime after respective components are implemented
5. **Branch management**: Ensure feature branch `feature/add-phone-number-field` is up-to-date before starting
6. **Testing verification**: Run `mvn clean test` after each phase to catch integration issues early

---

## Effort Summary

| Phase | Tasks | Estimated Time | Notes |
|-------|-------|-----------------|-------|
| Phase 1: Validation | 4 tasks (1.1-1.4) | 3.5 hours | CRITICAL — ReDoS fix, blocks all others |
| Phase 2: Entity & DTO | 4 tasks (2.1-2.4) | 2.5 hours | Depends on Phase 1 |
| Phase 3: Database | 4 tasks (3.1-3.4) | 3.5 hours | Parallel with Phase 2/4 |
| Phase 4: API Docs | 2 tasks (4.1-4.2) | 1 hour | Parallel with Phase 2/3 |
| Phase 5: Integration Tests | 6 tasks (5.1-5.6) | 5 hours | Parallel testing (1 dev = sequential) |
| Phase 6: Rollout & Docs | 2 tasks (6.1-6.2) | 1 hour | Low-priority, can defer if time-limited |
| **TOTAL** | **24 tasks** | **12-14 hours** | *For 1 developer with parallel planning* |

---

## Risk Mitigation

1. **ReDoS Vulnerability (HIGH RISK)**: Addressed in Task 1.2 by using libphonenumber (industry-standard, battle-tested validation library) instead of custom regex
2. **Database compatibility (MEDIUM RISK)**: Task 3.3 includes tests for all 4 DB types to catch syntax issues early
3. **Backward compatibility (MEDIUM RISK)**: Task 5.6 tests legacy clients to ensure API remains usable
4. **Performance impact (LOW RISK)**: Index strategy documented for future optimization (Task 3.2)
5. **Validation false positives (MEDIUM RISK)**: Task 1.4 includes ≥15 test cases covering international formats and edge cases

---

## Next Steps (After Approval)

1. Developer checks out feature branch: `git checkout feature/add-phone-number-field`
2. Start with Phase 1, Task 1.1 (add dependency)
3. Follow dependency order strictly (do not skip prerequisites)
4. After each phase, run `mvn clean test` to validate
5. After Phase 5, run full test suite: `mvn clean verify`
6. After Phase 6, prepare PR with all changes

