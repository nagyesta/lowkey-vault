package com.github.nagyesta.lowkeyvault.http.management;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RecoveryLevelTest {

    public static Stream<Arguments> validProvider() {
        final var list = Arrays.stream(RecoveryLevel.values())
                .map(r -> Arguments.of(r.getValue(), r))
                .collect(Collectors.toCollection(ArrayList::new));
        list.add(Arguments.of(null, RecoveryLevel.PURGEABLE));
        return list.stream();
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void testForValueShouldReturnExpectedValueWhenCalled(
            final String input,
            final RecoveryLevel expected) {
        //given

        //when
        final var actual = RecoveryLevel.forValue(input);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
