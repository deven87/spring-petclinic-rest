# Requirements: Add Phone Number Field to Pet Owner

## Overview
Pet owners need a way to store and update their phone number in the system to enable veterinarians to contact owners directly regarding their pets' appointments and health updates. A new `phoneNumber` field will be added to the `Owner` entity with input validation and exposed through the existing REST API.

## Functional Requirements
FR1: Add a `phoneNumber` field to the `Owner` entity to store owner contact information  
FR2: Validate phone number format on input to accept common international and US phone formats (e.g., +1-234-567-8900, (234) 567-8900, 234-567-8900)  
FR3: Expose the `phoneNumber` field in Owner REST API endpoints for GET, POST, and PUT operations  
FR4: Support phone number updates via REST API PUT endpoint `/api/owners/{id}`  
FR5: Persist phone number data to the database via JPA  
FR6: Map phone number field between `Owner` entity and `OwnerDTO` using the existing mapper pattern  

## Non-Functional Requirements
NFR1: Phone number field shall be optional (not mandatory for new or existing owners)  
NFR2: Validation shall use Spring validation annotations (e.g., `@Pattern` with regex or `@NotBlank`)  
NFR3: Phone number shall not be encrypted or masked (required for operational use by veterinarians)  
NFR4: Database column (`phone_number`) shall support up to 20 characters to accommodate international phone formats  
NFR5: Phone numbers shall not be constrained to be unique (multiple owners may share contact numbers)  
NFR6: OpenAPI/Swagger documentation shall be updated to include the `phoneNumber` field in the Owner schema  
NFR7: Database migration script shall be created to add the new column to existing schema(s) (H2, HSQLDB, MySQL, PostgreSQL)  

## Acceptance Criteria
AC1: Given an Owner without a phone number, When the phoneNumber field is left empty or null, Then the Owner is successfully created/updated and persisted with null phoneNumber  
AC2: Given a valid phone number format (e.g., "+1-234-567-8900" or "234-567-8900"), When the phoneNumber is submitted via POST /api/owners or PUT /api/owners/{id}, Then it is accepted and persisted to the database  
AC3: Given an invalid phone number (e.g., "abc", "12345", or malformed input), When submitted via API, Then a validation error is returned with a descriptive message indicating the required format  
AC4: Given an existing Owner, When GET /api/owners or GET /api/owners/{id} is called, Then the response includes the phoneNumber field (either populated or null)  
AC5: Given an existing Owner with an updated phone number, When PUT /api/owners/{id} is called with a new valid phoneNumber, Then the phone number is successfully updated in the database and reflected in subsequent GET requests  
AC6: Given the `Owner` entity with a phoneNumber field, When the `OwnerMapper.toOwnerDTO()` method is invoked, Then the phoneNumber is correctly mapped to the OwnerDTO and included in JSON responses  
AC7: Given the OpenAPI specification, When viewing /api/swagger-ui.html or /openapi.yml, Then the Owner schema displays the phoneNumber field with appropriate type (string), format, and constraints documentation  

## Constraints & Assumptions
- **Assumption**: Phone number format is flexible and accepts international formats (not US-only) with support for common separators (+, -, parentheses, spaces)
- **Assumption**: Phone number is an optional field; existing owners without a phone number will have a NULL value in the database
- **Assumption**: Phone numbers are not unique across owners; uniqueness constraints are not applied
- **Assumption**: Database schema updates via migration scripts will target all supported databases: H2, HSQLDB, MySQL, and PostgreSQL
- **Assumption**: The existing REST controller and OwnerRepository require no modifications to expose the new field
- **Constraint**: Phone number field maximum length shall not exceed 20 characters
- **Constraint**: Validation shall occur at both the entity and DTO layer to ensure consistency
