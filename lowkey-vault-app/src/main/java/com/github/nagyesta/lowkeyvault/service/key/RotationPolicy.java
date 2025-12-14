package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.common.TimeAware;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;

public interface RotationPolicy extends ReadOnlyRotationPolicy, TimeAware {

    void setLifetimeActions(Map<LifetimeActionType, LifetimeAction> lifetimeActions);

    void setCreated(OffsetDateTime created);

    void setUpdated(OffsetDateTime updated);

    void setExpiryTime(Period expiryTime);

    void validate(@Nullable OffsetDateTime latestKeyVersionExpiry);
}
