package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_IN_10_MINUTES;
import static com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType.*;

class KeyLifetimeActionTriggerTest {

    private static final OffsetDateTime A_WEEK_AGO = NOW.minusDays(MINIMUM_THRESHOLD_BEFORE_EXPIRY);
    private static final OffsetDateTime IN_A_WEEK = NOW.plusDays(MINIMUM_THRESHOLD_BEFORE_EXPIRY);
    private static final OffsetDateTime IN_A_WEEK_AND_10_MINUTES = TIME_IN_10_MINUTES.plusDays(MINIMUM_THRESHOLD_BEFORE_EXPIRY);
    private static final Period PERIOD_A_WEEK = Period.ofDays(MINIMUM_THRESHOLD_BEFORE_EXPIRY);
    private static final Period PERIOD_28_DAYS = Period.ofDays(MINIMUM_EXPIRY_PERIOD_IN_DAYS);

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, LifetimeActionTriggerType.TIME_BEFORE_EXPIRY))
                .add(Arguments.of(Period.ZERO, null))
                .build();
    }


    public static Stream<Arguments> validProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(PERIOD_A_WEEK, TIME_AFTER_CREATE, A_WEEK_AGO, null, true))
                .add(Arguments.of(PERIOD_28_DAYS, TIME_AFTER_CREATE, A_WEEK_AGO, null, false))
                .add(Arguments.of(PERIOD_28_DAYS, TIME_AFTER_CREATE, A_WEEK_AGO, NOW, false))
                .add(Arguments.of(PERIOD_A_WEEK, TIME_BEFORE_EXPIRY, NOW, IN_A_WEEK, true))
                .add(Arguments.of(PERIOD_A_WEEK, TIME_BEFORE_EXPIRY, NOW, IN_A_WEEK_AND_10_MINUTES, false))
                .add(Arguments.of(PERIOD_28_DAYS, TIME_BEFORE_EXPIRY, NOW.minusYears(1), NOW.plusYears(1), false))
                .add(Arguments.of(PERIOD_28_DAYS, TIME_BEFORE_EXPIRY, NOW.minusYears(1), NOW.plusDays(1), true))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(final Period period, final LifetimeActionTriggerType type) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyLifetimeActionTrigger(period, type));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void shouldTrigger(final Period period, final LifetimeActionTriggerType type,
                       final OffsetDateTime created, final OffsetDateTime expiry,
                       final boolean expected) {
        //given
        final var underTest = new KeyLifetimeActionTrigger(period, type);

        //when
        final var actual = underTest.shouldTrigger(created, expiry);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testRotateAfterDaysShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new KeyLifetimeActionTrigger(PERIOD_28_DAYS, TIME_AFTER_CREATE);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.rotateAfterDays(null));

        //then + exception
    }
}
