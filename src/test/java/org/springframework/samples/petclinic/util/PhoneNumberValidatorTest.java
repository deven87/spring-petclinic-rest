package org.springframework.samples.petclinic.util;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.validation.ValidPhoneNumber;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for PhoneNumberValidator.
 *
 * Tests comprehensive phone number validation including:
 * - Valid international formats
 * - Invalid formats
 * - Null and empty handling
 * - ReDoS attack prevention
 * - Edge cases
 *
 * @author Spring PetClinic Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PhoneNumberValidator Tests")
class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidator();
        // Initialize with default country "US"
        ValidPhoneNumber annotation = mock(ValidPhoneNumber.class);
        org.mockito.Mockito.when(annotation.defaultCountry()).thenReturn("US");
        validator.initialize(annotation);
    }

    // ==================== Valid Phone Numbers ====================

    @Test
    @DisplayName("Should accept valid US phone number with dashes")
    void testValidUSPhoneNumberWithDashes() {
        assertTrue(validator.isValid("212-555-0173", context));
    }

    @Test
    @DisplayName("Should accept valid US phone number with parentheses")
    void testValidUSPhoneNumberWithParentheses() {
        assertTrue(validator.isValid("(415) 555-0173", context));
    }

    @Test
    @DisplayName("Should accept valid US phone number with spaces")
    void testValidUSPhoneNumberWithSpaces() {
        assertTrue(validator.isValid("415 555 0173", context));
    }

    @Test
    @DisplayName("Should accept valid US phone number plain digits")
    void testValidUSPhoneNumberPlainDigits() {
        assertTrue(validator.isValid("4155550173", context));
    }

    @Test
    @DisplayName("Should accept valid international format +1 234-567-8900")
    void testValidInternationalFormatUSPrefix() {
        assertTrue(validator.isValid("+1 234-567-8900", context));
    }

    @Test
    @DisplayName("Should accept valid UK phone number +44 20 7946 0958")
    void testValidUKPhoneNumber() {
        assertTrue(validator.isValid("+44 20 7946 0958", context));
    }

    @Test
    @DisplayName("Should accept valid German phone number +49 30 12345678")
    void testValidGermanPhoneNumber() {
        assertTrue(validator.isValid("+49 30 12345678", context));
    }

    @Test
    @DisplayName("Should accept valid French phone number +33 1 42 68 53 00")
    void testValidFrenchPhoneNumber() {
        assertTrue(validator.isValid("+33 1 42 68 53 00", context));
    }

    @Test
    @DisplayName("Should accept valid French phone number without spaces +33142685300")
    void testValidFrenchPhoneNumberNoSpaces() {
        assertTrue(validator.isValid("+33142685300", context));
    }

    @Test
    @DisplayName("Should accept valid phone number with + sign and digits")
    void testValidPhoneNumberWithPlus() {
        assertTrue(validator.isValid("+14155552671", context));
    }

    @Test
    @DisplayName("Should accept valid phone number with extensions (parentheses format)")
    void testValidPhoneNumberWithExtension() {
        assertTrue(validator.isValid("(415) 555-0173 x123", context));
    }

    @Test
    @DisplayName("Should accept valid long distance format")
    void testValidLongDistanceFormat() {
        assertTrue(validator.isValid("1-800-555-0173", context));
    }

    // ==================== Invalid Phone Numbers ====================

    @Test
    @DisplayName("Should reject invalid format 'invalid'")
    void testRejectInvalidFormat() {
        assertFalse(validator.isValid("invalid", context));
    }

    @Test
    @DisplayName("Should reject phone number with too few digits '123'")
    void testRejectTooFewDigits() {
        assertFalse(validator.isValid("123", context));
    }

    @Test
    @DisplayName("Should reject phone number starting with '+' but containing non-digits")
    void testRejectInvalidCharactersAfterPlus() {
        assertFalse(validator.isValid("+++++X", context));
    }

    @Test
    @DisplayName("Should reject letters without numbers 'abc-def'")
    void testRejectLettersOnly() {
        assertFalse(validator.isValid("abc-def", context));
    }

    @Test
    @DisplayName("Should reject empty string")
    void testRejectEmptyString() {
        assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("Should reject string with only spaces")
    void testRejectWhitespaceOnly() {
        assertFalse(validator.isValid("   ", context));
    }

    @Test
    @DisplayName("Should reject phone number with only symbols")
    void testRejectOnlySymbols() {
        assertFalse(validator.isValid("!@#$%", context));
    }

    // ==================== ReDoS Attack Prevention Tests ====================

    @Test
    @DisplayName("Should handle ReDoS attack pattern safely - repeated plus signs")
    void testReDoSAttackRepeatedPlus() {
        assertFalse(validator.isValid("++++++++++++++++++X", context));
    }

    @Test
    @DisplayName("Should handle ReDoS attack pattern - many dashes")
    void testReDoSAttackManyDashes() {
        assertFalse(validator.isValid("1234---56789---123456789", context));
    }

    @Test
    @DisplayName("Should handle ReDoS attack pattern - nested brackets")
    void testReDoSAttackNestedBrackets() {
        assertFalse(validator.isValid("((((((555) 123-4567))))", context));
    }

    @Test
    @DisplayName("Should handle ReDoS attack pattern - very long string")
    void testReDoSAttackVeryLongString() {
        String longString = "1".repeat(1000) + "X";
        assertFalse(validator.isValid(longString, context));
    }

    @Test
    @DisplayName("Should handle ReDoS attack pattern - alternating pattern")
    void testReDoSAttackAlternatingPattern() {
        assertFalse(validator.isValid("+-+-+-+-+-+-+-+-+-X", context));
    }

    // ==================== Null and Edge Cases ====================

    @Test
    @DisplayName("Should accept null value (optional field)")
    void testNullValueIsValid() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Should reject tab character only")
    void testRejectTabCharacter() {
        assertFalse(validator.isValid("\t", context));
    }

    @Test
    @DisplayName("Should reject newline character only")
    void testRejectNewlineCharacter() {
        assertFalse(validator.isValid("\n", context));
    }

    // ==================== Whitespace Handling ====================

    @Test
    @DisplayName("Should handle phone number with leading/trailing spaces")
    void testPhoneNumberWithLeadingTrailingSpaces() {
        assertTrue(validator.isValid("  (415) 555-0173  ", context));
    }

    @Test
    @DisplayName("Should handle phone number with multiple internal spaces")
    void testPhoneNumberWithMultipleInternalSpaces() {
        assertTrue(validator.isValid("415   555   0173", context));
    }

    // ==================== Special Country Codes ====================

    @Test
    @DisplayName("Should accept valid Australian phone number")
    void testValidAustralianPhoneNumber() {
        assertTrue(validator.isValid("+61 2 8999 9999", context));
    }

    @Test
    @DisplayName("Should accept valid Canadian phone number")
    void testValidCanadianPhoneNumber() {
        assertTrue(validator.isValid("+1 416 555 0123", context));
    }

    @Test
    @DisplayName("Should accept valid Japanese phone number")
    void testValidJapanesePhoneNumber() {
        assertTrue(validator.isValid("+81 3-1234-5678", context));
    }
}
