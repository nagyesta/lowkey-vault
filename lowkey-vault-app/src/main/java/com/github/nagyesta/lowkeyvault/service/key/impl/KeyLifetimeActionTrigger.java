package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.util.PeriodUtil;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.time.Period;

public record KeyLifetimeActionTrigger(
        @NonNull Period timePeriod,
        @NonNull LifetimeActionTriggerType triggerType) implements LifetimeActionTrigger {

    @Override
    public boolean shouldTrigger(
            final OffsetDateTime created,
            final OffsetDateTime expiry) {
        return triggerType.shouldTrigger(created, expiry, timePeriod);
    }

    @Override
    public long rotateAfterDays(@NonNull final Period expiryPeriod) {
        final long days;
        if (triggerType == LifetimeActionTriggerType.TIME_AFTER_CREATE) {
            days = PeriodUtil.asDays(timePeriod);
        } else {
            days = PeriodUtil.asDays(expiryPeriod) - PeriodUtil.asDays(timePeriod);
        }
        return days;
    }
}
