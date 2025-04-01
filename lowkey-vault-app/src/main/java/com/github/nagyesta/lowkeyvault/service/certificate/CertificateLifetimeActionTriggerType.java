package com.github.nagyesta.lowkeyvault.service.certificate;

import lombok.NonNull;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import static java.time.temporal.ChronoUnit.DAYS;

public enum CertificateLifetimeActionTriggerType {
    /**
     * Triggers a number of days before expiry.
     */
    DAYS_BEFORE_EXPIRY {
        @Override
        public void validate(
                final int validityMonths,
                final int value) {
            Assert.state(value > 0 && value <= MONTHLY_LIMIT * validityMonths,
                    "Value must be between 1 and validity_in_months multiplied by 27.");
        }

        @Override
        public long triggersAfterDays(
                @NonNull final OffsetDateTime validityStart,
                @NonNull final OffsetDateTime expiry,
                final int value) {
            return DAYS.between(validityStart, expiry) - value;
        }
    },
    /**
     * Triggers after a certain percentage of the validity is past.
     */
    LIFETIME_PERCENTAGE {
        @Override
        public void validate(
                final int validityMonths,
                final int value) {
            Assert.state(value > 0 && value < ONE_HUNDRED, "Value must be between 1 and 99.");
        }

        @Override
        public long triggersAfterDays(
                @NonNull final OffsetDateTime validityStart,
                @NonNull final OffsetDateTime expiry,
                final int value) {
            return onePercentInDays(validityStart, expiry).multiply(BigDecimal.valueOf(value)).longValue();
        }

        private BigDecimal onePercentInDays(
                final OffsetDateTime validityStart,
                final OffsetDateTime expiry) {
            return BigDecimal.valueOf(DAYS.between(validityStart, expiry), SCALE)
                    .divide(PERCENT, SCALE, RoundingMode.HALF_EVEN);
        }
    };

    private static final int ONE_HUNDRED = 100;
    private static final int MONTHLY_LIMIT = 27;
    private static final int SCALE = 2;
    private static final BigDecimal PERCENT = BigDecimal.valueOf(ONE_HUNDRED, SCALE);

    public abstract void validate(int validityMonths, int value);

    public abstract long triggersAfterDays(@NonNull OffsetDateTime validityStart, @NonNull OffsetDateTime expiry, int value);
}
