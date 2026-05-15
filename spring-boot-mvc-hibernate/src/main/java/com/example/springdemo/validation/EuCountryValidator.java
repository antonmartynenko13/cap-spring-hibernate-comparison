package com.example.springdemo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks that a string value matches one of the EuCountry enum constants (case-insensitive).
 * Null/blank values are considered valid (combine with @NotBlank if the field is required).
 *
 * CAP equivalent: type EuCountry : String enum in data-model.cds.
 * CAP automatically rejects values outside the enum via @assert.range – no handler needed.
 */
public class EuCountryValidator implements ConstraintValidator<EuCountry, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            com.example.springdemo.entity.EuCountry.valueOf(value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}