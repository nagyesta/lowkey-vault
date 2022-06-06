package com.github.nagyesta.lowkeyvault.service.key.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.stream.Stream;

class PeriodUtilTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("-P1D", -1))
                .add(Arguments.of("P0D", 0))
                .add(Arguments.of("P1D", 1))
                .add(Arguments.of("P5D", 5))
                .add(Arguments.of("P7D", 7))
                .add(Arguments.of("P2M10D", 71))
                .add(Arguments.of("P1Y1M1D", 397))
                .build();
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<PeriodUtil> constructor = PeriodUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsDaysShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> PeriodUtil.asDays(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void testAsDaysShouldReturnNumberOfDaysWhenCalledWithValidPeriods(final String period, final int expected) {
        //given
        final OffsetDateTime time = OffsetDateTime.of(2022, 5, 10, 0, 0, 0, 0, ZoneOffset.UTC);
        final Period parsed = Period.parse(period);

        //when
        final long actual = PeriodUtil.asDays(parsed, time);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
