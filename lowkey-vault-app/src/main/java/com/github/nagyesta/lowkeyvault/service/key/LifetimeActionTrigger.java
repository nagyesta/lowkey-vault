package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;

import java.time.OffsetDateTime;
import java.time.Period;

public interface LifetimeActionTrigger {

    Period timePeriod();

    LifetimeActionTriggerType triggerType();

    boolean shouldTrigger(OffsetDateTime created, OffsetDateTime expiry);

    long rotateAfterDays(Period expiryPeriod);
}
