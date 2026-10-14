package org.springframework.samples.petclinic.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.samples.petclinic.util.PhoneNumberValidator;

import java.lang.annotation.*;

/**
 * Validation annotation for phone numbers using libphonenumber library.
 * Validates phone numbers in multiple international formats.
 *
 * Can be applied to String fields. A null value is considered valid (optional field).
 * Empty strings are considered invalid.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {

    String message() default "Invalid phone number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Optional: default country code (e.g., "US", "GB", "DE")
     * Used when parsing phone numbers without explicit country prefix.
     */
    String defaultCountry() default "US";
}
