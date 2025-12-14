package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.TimeAware;
import lombok.Data;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

@Data
public class BaseLifetimePolicy<E extends EntityId> implements TimeAware {

    private final E id;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    protected BaseLifetimePolicy(final E id) {
        this.id = id;
        this.created = OffsetDateTime.now();
        this.updated = OffsetDateTime.now();
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        created = created.minusSeconds(offsetSeconds);
        updated = updated.minusSeconds(offsetSeconds);
    }

    protected void markUpdate() {
        updated = OffsetDateTime.now();
    }

    protected List<OffsetDateTime> collectMissedTriggerDays(
            final ToLongFunction<OffsetDateTime> triggerAfterDaysFunction,
            final OffsetDateTime startPoint) {
        final var now = OffsetDateTime.now(ZoneOffset.UTC);
        final List<OffsetDateTime> rotationTimes = new ArrayList<>();
        var latestDay = startPoint;
        while (latestDay.plusDays(triggerAfterDaysFunction.applyAsLong(latestDay)).isBefore(now)) {
            latestDay = latestDay.plusDays(triggerAfterDaysFunction.applyAsLong(latestDay));
            rotationTimes.add(latestDay);
        }
        return List.copyOf(rotationTimes);
    }

    protected OffsetDateTime findTriggerTimeOffset(
            final OffsetDateTime entityCreation,
            final ToLongFunction<OffsetDateTime> triggerAfterDaysFunction) {
        final var daysUntilTrigger = triggerAfterDaysFunction.applyAsLong(created);
        final var relativeToLifetimeActionPolicy = created.minusDays(daysUntilTrigger);
        var startPoint = entityCreation;
        if (entityCreation.isBefore(relativeToLifetimeActionPolicy)) {
            startPoint = relativeToLifetimeActionPolicy;
        }
        return startPoint;
    }
}
