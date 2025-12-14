package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseLifetimePolicy;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class KeyRotationPolicy extends BaseLifetimePolicy<KeyEntityId> implements RotationPolicy {

    private Period expiryTime;
    private Map<LifetimeActionType, LifetimeAction> lifetimeActions;

    public KeyRotationPolicy(
            final KeyEntityId keyEntityId,
            final Period expiryTime,
            final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
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
    public List<OffsetDateTime> missedRotations(final OffsetDateTime keyCreation) {
        Assert.isTrue(isAutoRotate(), "Cannot have missed rotations without a \"rotate\" lifetime action.");
        final var trigger = lifetimeActions.get(LifetimeActionType.ROTATE).trigger();
        final var startPoint = findTriggerTimeOffset(keyCreation, s -> trigger.rotateAfterDays(expiryTime));
        return collectMissedTriggerDays(s -> trigger.rotateAfterDays(expiryTime), startPoint);
    }

    @Override
    public Map<LifetimeActionType, LifetimeAction> getLifetimeActions() {
        return lifetimeActions;
    }

    @Override
    public void validate(@Nullable final OffsetDateTime latestKeyVersionExpiry) {
        lifetimeActions.values().forEach(action -> {
            final var triggerPeriod = action.trigger().timePeriod();
            final var triggerType = action.trigger().triggerType();
            triggerType.validate(latestKeyVersionExpiry, expiryTime, triggerPeriod);
            Assert.isTrue(action.actionType() != LifetimeActionType.NOTIFY
                            || triggerType == LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                    "Notify actions cannot be used with time after creation trigger.");
        });
    }

    @Override
    public void setExpiryTime(final Period expiryTime) {
        this.expiryTime = expiryTime;
        this.markUpdate();
    }

    @Override
    public void setLifetimeActions(
            final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        Assert.isTrue(notifyIsNotRemoved(lifetimeActions), "Notify action cannot be removed.");
        this.lifetimeActions = Map.copyOf(lifetimeActions);
        this.markUpdate();
    }

    private boolean notifyIsNotRemoved(final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        return !this.lifetimeActions.containsKey(LifetimeActionType.NOTIFY)
                || lifetimeActions.containsKey(LifetimeActionType.NOTIFY);
    }
}
