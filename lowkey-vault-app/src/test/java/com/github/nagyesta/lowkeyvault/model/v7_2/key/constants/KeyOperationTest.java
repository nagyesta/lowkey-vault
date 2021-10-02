package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class KeyOperationTest {

    public static Stream<Arguments> valueProvider() {
        final List<KeyOperation> list = new ArrayList<>();
        list.add(null);
        list.addAll(Arrays.asList(KeyOperation.values()));
        return list.stream()
                .map(value -> Arguments.of(Optional.ofNullable(value).map(KeyOperation::getValue).orElse(null), value));
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testForValueShouldReturnEnumWhenValueStringMatches(final String input, final KeyOperation expected) {
        //given

        //when
        final KeyOperation actual = KeyOperation.forValue(input);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
