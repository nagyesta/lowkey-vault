package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.time.Period;

public class KeyLifetimeActionTrigger implements LifetimeActionTrigger {

    private final Period timePeriod;
    private final LifetimeActionTriggerType triggerType;

    public KeyLifetimeActionTrigger(@NonNull final Period timePeriod, @NonNull final LifetimeActionTriggerType triggerType) {
        this.timePeriod = timePeriod;
        this.triggerType = triggerType;
    }

    @Override
    public Period getTimePeriod() {
        return timePeriod;
    }

    @Override
    public LifetimeActionTriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public boolean shouldTrigger(final OffsetDateTime created, final OffsetDateTime expiry) {
        return triggerType.shouldTrigger(created, expiry, timePeriod);
    }
}
