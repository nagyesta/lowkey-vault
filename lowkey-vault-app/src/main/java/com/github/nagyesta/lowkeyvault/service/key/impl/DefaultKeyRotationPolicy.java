package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;

import java.time.Period;
import java.util.Map;

public class DefaultKeyRotationPolicy extends KeyRotationPolicy {

    private static final KeyLifetimeActionTrigger TRIGGER_30_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeActionTrigger(Period.ofDays(30), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);
    private static final KeyLifetimeAction NOTIFY_30_DAYS_BEFORE_EXPIRY
            = new KeyLifetimeAction(LifetimeActionType.NOTIFY, TRIGGER_30_DAYS_BEFORE_EXPIRY);
    private static final Period PERIOD_1_YEAR = Period.ofYears(1);

    public DefaultKeyRotationPolicy(final KeyEntityId keyEntityId) {
        super(keyEntityId, PERIOD_1_YEAR, Map.of(LifetimeActionType.NOTIFY, NOTIFY_30_DAYS_BEFORE_EXPIRY));
    }
}
