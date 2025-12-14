package com.github.nagyesta.lowkeyvault.service.key.constants;

import com.github.nagyesta.lowkeyvault.service.key.util.PeriodUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;

public enum LifetimeActionTriggerType {

    /**
     * Triggers an action relative to the creation of the key.
     */
    TIME_AFTER_CREATE {
        @Override
        public boolean shouldTrigger(
                @Nullable final OffsetDateTime createTime,
                @Nullable final OffsetDateTime expiryTime,
                final Period triggerPeriod) {
            Assert.notNull(createTime, "Creation time is not set, after create triggers are not allowed.");
            return createTime.plusDays(PeriodUtil.asDays(triggerPeriod)).isBefore(OffsetDateTime.now());
        }

        @Override
        public void validate(
                @Nullable final OffsetDateTime expiryTime,
                final Period expiryPeriod,
                final Period triggerPeriod) {
            super.validate(expiryTime, expiryPeriod, triggerPeriod);
            final var threshold = expiryPeriod.minusDays(MINIMUM_THRESHOLD_BEFORE_EXPIRY);
            Assert.isTrue(PeriodUtil.asDays(threshold) >= PeriodUtil.asDays(triggerPeriod),
                    "Trigger must be at least " + MINIMUM_THRESHOLD_BEFORE_EXPIRY + " days before expiry.");
        }
    },

    /**
     * Triggers an action relative to the expiry of the key.
     */
    TIME_BEFORE_EXPIRY {
        @Override
        public boolean shouldTrigger(
                @Nullable final OffsetDateTime createTime,
                @Nullable final OffsetDateTime expiryTime,
                final Period triggerPeriod) {
            Assert.notNull(expiryTime, "Expiry time is not set, before expiry triggers are not allowed.");
            return expiryTime.minusDays(PeriodUtil.asDays(triggerPeriod)).isBefore(OffsetDateTime.now());
        }

        @Override
        public void validate(
                @Nullable final OffsetDateTime expiryTime,
                final Period expiryPeriod,
                final Period triggerPeriod) {
            super.validate(expiryTime, expiryPeriod, triggerPeriod);
            Assert.notNull(expiryTime, "Expiry time is not set, before expiry triggers are not allowed.");
            Assert.isTrue(PeriodUtil.asDays(triggerPeriod) >= MINIMUM_THRESHOLD_BEFORE_EXPIRY,
                    "Trigger must be at least " + MINIMUM_THRESHOLD_BEFORE_EXPIRY + " days before expiry.");
        }
    };

    /**
     * Minimum number of days we need to leave for a trigger action after creation and before expiry.
     */
    public static final int MINIMUM_THRESHOLD_BEFORE_EXPIRY = 7;
    /**
     * Minimum number of days needed for expiry periods.
     */
    public static final int MINIMUM_EXPIRY_PERIOD_IN_DAYS = 28;

    public abstract boolean shouldTrigger(
            @Nullable OffsetDateTime createTime,
            @Nullable OffsetDateTime expiryTime,
            Period triggerPeriod);

    @SuppressWarnings("java:S1172") //the subclasses need these parameters for their implementation
    public void validate(
            @Nullable final OffsetDateTime expiryTime,
            final Period expiryPeriod,
            final Period triggerPeriod) {
        Assert.isTrue(PeriodUtil.asDays(expiryPeriod) >= MINIMUM_EXPIRY_PERIOD_IN_DAYS,
                "Expiry period must be at least " + MINIMUM_EXPIRY_PERIOD_IN_DAYS + " days.");
    }
}
