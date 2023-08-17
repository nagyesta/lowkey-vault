package com.github.nagyesta.lowkeyvault.model.v7_2.key.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ImportKeyValidator.class)
public @interface ValidImportKey {

    String message() default "Json Web Key import request is invalid.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
