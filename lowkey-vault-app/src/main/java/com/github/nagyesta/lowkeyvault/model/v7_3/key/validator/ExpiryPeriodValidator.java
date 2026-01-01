package com.github.nagyesta.lowkeyvault.model.v7_3.key.validator;

import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.util.PeriodUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.time.Period;
import java.util.Optional;

public class ExpiryPeriodValidator implements ConstraintValidator<ExpiryPeriod, Period> {

    @Override
    public void initialize(final ExpiryPeriod constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(
            @Nullable final Period value,
            final ConstraintValidatorContext context) {
        return Optional.ofNullable(value)
                .map(PeriodUtil::asDays)
                .map(totalDays -> totalDays >= LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS)
                .orElse(true);
    }
}
