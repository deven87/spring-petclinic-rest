# Phone Number Feature Documentation

## Overview
This document describes the Phone Number feature added to the Spring PetClinic REST API.

## Feature Description
The phone number field (`phoneNumber`) has been added to the Owner entity to support storing additional contact information for pet owners. This field complements the existing `telephone` field and allows for more flexible phone number storage.

## Technical Details

### Database Schema
- **Field Name**: `phone_number`
- **Type**: `VARCHAR(20)`
- **Nullable**: `true` (optional field)
- **Column added to**: `owners` table

The schema migrations have been applied to all supported databases:
- H2 Database
- HSQLDB
- MySQL
- PostgreSQL

### API Contract
#### OpenAPI Schema Definition
```yaml
phoneNumber:
  type: string
  minLength: 10
  maxLength: 20
  pattern: '^[0-9+\-\(\)\s\.]+$'
  example: '+1-234-567-8900'
  description: 'Phone number in international or local format (10-20 chars, optional)'
  nullable: true
```

### Field Specifications
- **Format**: Supports international and local formats
- **Pattern**: Allows digits, plus sign (+), hyphens (-), parentheses (), spaces, and dots (.)
- **Length**: 10-20 characters
- **Validation**: Optional field (nullable)
- **Examples**: 
  - `+1-234-567-8900` (International format)
  - `(123) 456-7890` (US format with parentheses)
  - `123-456-7890` (Hyphenated format)

## REST Endpoints

### Create Owner with Phone Number
```http
POST /api/owners
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "address": "123 Main St.",
  "city": "Springfield",
  "telephone": "1234567890",
  "phoneNumber": "+1-234-567-8900"
}
```

### Get Owner (includes phoneNumber)
```http
GET /api/owners/{id}

Response:
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "address": "123 Main St.",
  "city": "Springfield",
  "telephone": "1234567890",
  "phoneNumber": "+1-234-567-8900",
  "pets": []
}
```

### Update Owner's Phone Number
```http
PUT /api/owners/{id}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "address": "123 Main St.",
  "city": "Springfield",
  "telephone": "1234567890",
  "phoneNumber": "+1-555-555-9999"
}
```

## Testing
Integration tests have been created to verify the phone number functionality:
- `OwnerPhoneNumberIntegrationTest.testCreateOwnerWithPhoneNumber()` - Verifies POST /api/owners with phoneNumber
- `OwnerPhoneNumberIntegrationTest.testGetOwnerReturnsPhoneNumber()` - Verifies GET /api/owners/{id} returns phoneNumber
- `OwnerPhoneNumberIntegrationTest.testUpdateOwnerPhoneNumber()` - Verifies PUT /api/owners/{id} updates phoneNumber

Run tests with: `mvn test -Dtest=OwnerPhoneNumberIntegrationTest`

## Migration Notes
When upgrading to this version, the database schema will be automatically updated to include the new `phone_number` column in the `owners` table. No data migration is required as the field is optional and defaults to NULL for existing owners.

## Backward Compatibility
This feature maintains full backward compatibility:
- The `phoneNumber` field is optional
- Existing owners without a phone number value will have NULL
- The `telephone` field remains unchanged
- All existing API functionality continues to work as before
