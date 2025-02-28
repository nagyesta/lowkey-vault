package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.TimeAware;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
public class BaseLifetimePolicy<E extends EntityId> implements TimeAware {

    private final E id;
    @NonNull
    private OffsetDateTime createdOn;
    @NonNull
    private OffsetDateTime updatedOn;

    protected BaseLifetimePolicy(@NonNull final E id) {
        this.id = id;
        this.createdOn = OffsetDateTime.now();
        this.updatedOn = OffsetDateTime.now();
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        createdOn = createdOn.minusSeconds(offsetSeconds);
        updatedOn = updatedOn.minusSeconds(offsetSeconds);
    }

    protected void markUpdate() {
        updatedOn = OffsetDateTime.now();
    }

    protected List<OffsetDateTime> collectMissedTriggerDays(
            final Function<OffsetDateTime, Long> triggerAfterDaysFunction,
            final OffsetDateTime startPoint) {
        final var now = OffsetDateTime.now(ZoneOffset.UTC);
        final List<OffsetDateTime> rotationTimes = new ArrayList<>();
        var latestDay = startPoint;
        while (latestDay.plusDays(triggerAfterDaysFunction.apply(latestDay)).isBefore(now)) {
            latestDay = latestDay.plusDays(triggerAfterDaysFunction.apply(latestDay));
            rotationTimes.add(latestDay);
        }
        return List.copyOf(rotationTimes);
    }

    protected OffsetDateTime findTriggerTimeOffset(
            final OffsetDateTime entityCreation,
            final Function<OffsetDateTime, Long> triggerAfterDaysFunction) {
        final var relativeToLifetimeActionPolicy = createdOn.minusDays(triggerAfterDaysFunction.apply(createdOn));
        var startPoint = entityCreation;
        if (entityCreation.isBefore(relativeToLifetimeActionPolicy)) {
            startPoint = relativeToLifetimeActionPolicy;
        }
        return startPoint;
    }
}
