package com.github.nagyesta.lowkeyvault.service.certificate.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;

class ParserUtilTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> monthProvider() {
        return IntStream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 24, 36, 42, 120, 240, 360)
                .mapToObj(Arguments::of);
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<ParserUtil> constructor = ParserUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @ParameterizedTest
    @MethodSource("monthProvider")
    void testCalculateValidityMonthsShouldReturnExpectedNumberWhenCalledWithValidValues(final int realMonths) {
        //given
        final OffsetDateTime from = NOW;
        final OffsetDateTime to = NOW.plusMonths(realMonths);

        //when
        final int actual = ParserUtil.calculateValidityMonths(from.toInstant(), to.toInstant());

        //then
        Assertions.assertEquals(realMonths, actual);
    }
}
