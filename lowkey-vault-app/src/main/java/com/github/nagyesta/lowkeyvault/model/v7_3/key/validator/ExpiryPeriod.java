package com.github.nagyesta.lowkeyvault.model.v7_3.key.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ExpiryPeriodValidator.class)
public @interface ExpiryPeriod {

    String message() default "Expiry period is invalid.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
