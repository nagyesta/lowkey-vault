package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.common.TimeAware;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Map;

public interface RotationPolicy extends ReadOnlyRotationPolicy, TimeAware {

    void setLifetimeActions(Map<LifetimeActionType, LifetimeAction> lifetimeActions);

    void setCreatedOn(OffsetDateTime createdOn);

    void setUpdatedOn(OffsetDateTime updatedOn);

    void setExpiryTime(Period expiryTime);

    void validate(OffsetDateTime latestKeyVersionExpiry);
}
