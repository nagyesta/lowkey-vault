package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyRotationPolicy implements RotationPolicy {

    private final KeyEntityId keyEntityId;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private Period expiryTime;
    private Map<LifetimeActionType, LifetimeAction> lifetimeActions;

    public KeyRotationPolicy(@NonNull final KeyEntityId keyEntityId,
                             @NonNull final Period expiryTime,
                             @NonNull final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        this.keyEntityId = keyEntityId;
        this.expiryTime = expiryTime;
        this.lifetimeActions = Map.copyOf(lifetimeActions);
        this.createdOn = OffsetDateTime.now();
        this.updatedOn = OffsetDateTime.now();
    }

    @Override
    public KeyEntityId getId() {
        return keyEntityId;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
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
        final OffsetDateTime startPoint = findRotationTimeOffset(keyCreation, rotateAfterDays);
        return collectMissedRotationDays(rotateAfterDays, startPoint);
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
    public void setCreatedOn(@NonNull final OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public void setUpdatedOn(@NonNull final OffsetDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public void setExpiryTime(@NonNull final Period expiryTime) {
        this.expiryTime = expiryTime;
        this.updatedOn = OffsetDateTime.now();
    }

    @Override
    public void setLifetimeActions(@NonNull final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        Assert.isTrue(notifyIsNotRemoved(lifetimeActions), "Notify action cannot be removed.");
        this.lifetimeActions = Map.copyOf(lifetimeActions);
        this.updatedOn = OffsetDateTime.now();
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        createdOn = createdOn.minusSeconds(offsetSeconds);
        updatedOn = updatedOn.minusSeconds(offsetSeconds);
    }

    private List<OffsetDateTime> collectMissedRotationDays(final long rotateAfterDays, final OffsetDateTime startPoint) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final List<OffsetDateTime> rotationTimes = new ArrayList<>();
        for (int i = 1; startPoint.plusDays(i * rotateAfterDays).isBefore(now); i++) {
            rotationTimes.add(startPoint.plusDays(i * rotateAfterDays));
        }
        return List.copyOf(rotationTimes);
    }

    private OffsetDateTime findRotationTimeOffset(final OffsetDateTime keyCreation, final long rotateAfterDays) {
        final OffsetDateTime relativeToRotationPolicy = createdOn.minusDays(rotateAfterDays);
        OffsetDateTime startPoint = keyCreation;
        if (keyCreation.isBefore(relativeToRotationPolicy)) {
            startPoint = relativeToRotationPolicy;
        }
        return startPoint;
    }

    private boolean notifyIsNotRemoved(final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        return !this.lifetimeActions.containsKey(LifetimeActionType.NOTIFY)
                || lifetimeActions.containsKey(LifetimeActionType.NOTIFY);
    }
}
