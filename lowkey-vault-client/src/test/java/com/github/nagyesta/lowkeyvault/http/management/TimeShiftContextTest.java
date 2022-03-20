package com.github.nagyesta.lowkeyvault.http.management;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeShiftContextTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @ValueSource(ints = {-42, -10, -5, -3, -2, -1, 0})
    void testBuilderShouldThrowExceptionWhenCalledWithNegativeValue(final int value) {
        //given
        //noinspection WriteOnlyObject
        final TimeShiftContext.TimeShiftContextBuilder underTest = TimeShiftContext.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addSeconds(value));
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addMinutes(value));
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addHours(value));
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addDays(value));

        //then + exception
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullUri() {
        //given
        //noinspection WriteOnlyObject
        final TimeShiftContext.TimeShiftContextBuilder underTest = TimeShiftContext.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.vaultBaseUri(null));

        //then + exception
    }
}
