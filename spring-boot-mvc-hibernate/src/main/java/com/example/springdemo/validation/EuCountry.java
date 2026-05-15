package com.example.springdemo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that the annotated String is a European Union member state name.
 * Null and blank values pass – combine with @NotBlank if the field is required.
 *
 * CAP equivalent: @Before handler in UserServiceHandler performing the same check.
 */
@Documented
@Constraint(validatedBy = EuCountryValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EuCountry {

    String message() default "country must be a European Union member state";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}