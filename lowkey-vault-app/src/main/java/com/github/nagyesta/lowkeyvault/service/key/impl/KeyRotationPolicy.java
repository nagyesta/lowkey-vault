package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseLifetimePolicy;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;

public class KeyRotationPolicy extends BaseLifetimePolicy<KeyEntityId> implements RotationPolicy {
    private Period expiryTime;
    private Map<LifetimeActionType, LifetimeAction> lifetimeActions;

    public KeyRotationPolicy(@org.springframework.lang.NonNull final KeyEntityId keyEntityId,
                             @NonNull final Period expiryTime,
                             @NonNull final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        super(keyEntityId);
        this.expiryTime = expiryTime;
        this.lifetimeActions = Map.copyOf(lifetimeActions);
    }

    @Override
    public Period getExpiryTime() {
        return expiryTime;
    }

    @Override
    public boolean isAutoRotate() {
        return lifetimeActions.containsKey(LifetimeActionType.ROTATE);
    }

    @Override
    public List<OffsetDateTime> missedRotations(@NonNull final OffsetDateTime keyCreation) {
        Assert.isTrue(isAutoRotate(), "Cannot have missed rotations without a \"rotate\" lifetime action.");
        final long rotateAfterDays = lifetimeActions.get(LifetimeActionType.ROTATE).getTrigger().rotateAfterDays(expiryTime);
        final OffsetDateTime startPoint = findTriggerTimeOffset(keyCreation, rotateAfterDays);
        return collectMissedTriggerDays(rotateAfterDays, startPoint);
    }

    @Override
    public Map<LifetimeActionType, LifetimeAction> getLifetimeActions() {
        return lifetimeActions;
    }

    @Override
    public void validate(final OffsetDateTime latestKeyVersionExpiry) {
        lifetimeActions.values().forEach(action -> {
            final Period triggerPeriod = action.getTrigger().getTimePeriod();
            final LifetimeActionTriggerType triggerType = action.getTrigger().getTriggerType();
            triggerType.validate(latestKeyVersionExpiry, expiryTime, triggerPeriod);
            Assert.isTrue(action.getActionType() != LifetimeActionType.NOTIFY
                            || triggerType == LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                    "Notify actions cannot be used with time after creation trigger.");
        });
    }

    @Override
    public void setExpiryTime(@NonNull final Period expiryTime) {
        this.expiryTime = expiryTime;
        this.markUpdate();
    }

    @Override
    public void setLifetimeActions(@NonNull final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        Assert.isTrue(notifyIsNotRemoved(lifetimeActions), "Notify action cannot be removed.");
        this.lifetimeActions = Map.copyOf(lifetimeActions);
        this.markUpdate();
    }

    private boolean notifyIsNotRemoved(final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        return !this.lifetimeActions.containsKey(LifetimeActionType.NOTIFY)
                || lifetimeActions.containsKey(LifetimeActionType.NOTIFY);
    }
}
