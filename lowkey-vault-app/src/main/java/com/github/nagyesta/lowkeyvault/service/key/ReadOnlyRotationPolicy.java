package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;

public interface ReadOnlyRotationPolicy {

    KeyEntityId getId();

    OffsetDateTime getCreatedOn();

    OffsetDateTime getUpdatedOn();

    Period getExpiryTime();

    Map<LifetimeActionType, LifetimeAction> getLifetimeActions();

}
