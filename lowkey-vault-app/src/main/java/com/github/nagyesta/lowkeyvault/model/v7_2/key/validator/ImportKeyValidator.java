package com.github.nagyesta.lowkeyvault.model.v7_2.key.validator;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ImportKeyValidator implements ConstraintValidator<ValidImportKey, JsonWebKeyImportRequest> {

    private final Validator validator;

    @Autowired
    public ImportKeyValidator(final Validator validator) {
        this.validator = validator;
    }

    @Override
    public void initialize(final ValidImportKey constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(final JsonWebKeyImportRequest value, final ConstraintValidatorContext context) {
        final Class<? extends BaseKey> group = findGroup(value.getKeyType());
        final Set<ConstraintViolation<JsonWebKeyImportRequest>> violations = validator.validate(value, group);
        violations.forEach(v -> context.buildConstraintViolationWithTemplate(v.getMessageTemplate())
                .addPropertyNode(v.getPropertyPath().toString())
                .addConstraintViolation());
        return violations.isEmpty();
    }

    private Class<? extends BaseKey> findGroup(final KeyType value) {
        Class<? extends BaseKey> group = BaseKey.class;
        if (value != null) {
            if (value.isEc()) {
                group = EcKey.class;
            } else if (value.isRsa()) {
                group = RsaKey.class;
            } else {
                group = OctKey.class;
            }
        }
        return group;
    }
}
