package org.springframework.samples.petclinic.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.validation.ValidPhoneNumber;

/**
 * Custom ConstraintValidator for phone number validation using libphonenumber library.
 *
 * This validator uses Google's libphonenumber library to safely parse and validate
 * phone numbers in multiple international formats. It prevents ReDoS (Regular Expression Denial of Service)
 * attacks by avoiding custom regex patterns.
 *
 * Validation rules:
 * - null values are considered valid (field is optional)
 * - empty strings are considered invalid
 * - phone numbers are validated using libphonenumber's production-grade validation
 *
 * @author Spring PetClinic Team
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Logger logger = LoggerFactory.getLogger(PhoneNumberValidator.class);
    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    private String defaultCountry;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.defaultCountry = constraintAnnotation.defaultCountry();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are considered valid (field is optional)
        if (value == null) {
            return true;
        }

        // Empty strings are considered invalid
        if (value.trim().isEmpty()) {
            logInvalidPhoneNumber(value, "Empty phone number");
            return false;
        }

        try {
            // Parse the phone number using the default country code
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(value, defaultCountry);

            // Validate the parsed phone number
            boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);

            if (!isValid) {
                logInvalidPhoneNumber(value, "Failed validation check");
            }

            return isValid;

        } catch (NumberParseException e) {
            // Log the parsing error with masked phone number (for security)
            logInvalidPhoneNumber(value, "Parse error: " + e.getErrorType());
            return false;
        } catch (Exception e) {
            // Catch any unexpected exceptions
            logger.warn("Unexpected error validating phone number: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Logs failed phone number validation with masked phone number for security.
     *
     * @param phoneNumber the phone number that failed validation
     * @param reason      the reason for failure
     */
    private void logInvalidPhoneNumber(String phoneNumber, String reason) {
        String masked = maskPhoneNumber(phoneNumber);
        logger.warn("Invalid phone number detected [{}]: {}", masked, reason);
    }

    /**
     * Masks the phone number for logging purposes (shows only first and last digits).
     *
     * @param phoneNumber the phone number to mask
     * @return the masked phone number
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return "****";
        }
        String first = phoneNumber.substring(0, 2);
        String last = phoneNumber.substring(phoneNumber.length() - 2);
        return first + "****" + last;
    }
}
