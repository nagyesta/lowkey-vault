package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.util.PeriodUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;

public record KeyLifetimeActionTrigger(
        Period timePeriod,
        LifetimeActionTriggerType triggerType) implements LifetimeActionTrigger {

    @Override
    public boolean shouldTrigger(
            @Nullable final OffsetDateTime created,
            @Nullable final OffsetDateTime expiry) {
        return triggerType.shouldTrigger(created, expiry, timePeriod);
    }

    @Override
    public long rotateAfterDays(final Period expiryPeriod) {
        Assert.notNull(expiryPeriod, "Expiry period cannot be null.");
        final long days;
        if (triggerType == LifetimeActionTriggerType.TIME_AFTER_CREATE) {
            days = PeriodUtil.asDays(timePeriod);
        } else {
            days = PeriodUtil.asDays(expiryPeriod) - PeriodUtil.asDays(timePeriod);
        }
        return days;
    }
}
