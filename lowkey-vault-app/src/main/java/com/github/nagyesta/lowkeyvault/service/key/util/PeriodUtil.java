package com.github.nagyesta.lowkeyvault.service.key.util;

import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public final class PeriodUtil {

    private PeriodUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static long asDays(final Period period) {
        Assert.notNull(period, "Period cannot be null.");
        return asDays(period, OffsetDateTime.now());
    }

    static long asDays(
            final Period period,
            final OffsetDateTime relativeTo) {
        return ChronoUnit.DAYS.between(relativeTo, relativeTo.plus(period));
    }
}
