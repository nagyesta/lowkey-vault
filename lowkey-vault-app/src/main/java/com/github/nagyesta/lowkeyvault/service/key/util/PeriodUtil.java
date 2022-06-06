package com.github.nagyesta.lowkeyvault.service.key.util;

import lombok.NonNull;

import java.time.OffsetDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public final class PeriodUtil {

    private PeriodUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static long asDays(@NonNull final Period period) {
        return asDays(period, OffsetDateTime.now());
    }

    static long asDays(final Period period, final OffsetDateTime relativeTo) {
        return ChronoUnit.DAYS.between(relativeTo, relativeTo.plus(period));
    }
}
