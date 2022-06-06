package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;

import java.time.OffsetDateTime;
import java.time.Period;

public interface LifetimeActionTrigger {

    Period getTimePeriod();

    LifetimeActionTriggerType getTriggerType();

    boolean shouldTrigger(OffsetDateTime created, OffsetDateTime expiry);
}
