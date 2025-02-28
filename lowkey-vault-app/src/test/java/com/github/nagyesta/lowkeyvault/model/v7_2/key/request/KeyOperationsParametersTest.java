package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

class KeyOperationsParametersTest {

    public static Stream<Arguments> base64Provider() {
        final var encoder = Base64.getUrlEncoder().withoutPadding();
        return Stream.of(null, EMPTY, BLANK, LOCALHOST)
                .map(s -> Optional.ofNullable(s).map(String::getBytes).orElse(null))
                .map(b -> Arguments.of(Optional.ofNullable(b).map(encoder::encodeToString).orElse(null), b));
    }

    @ParameterizedTest
    @MethodSource("base64Provider")
    void testGtValueAsBase64DecodedBytesShouldReturnBase64DecodedValueWhenCalledWithString(
            final String input, final byte[] expected) {
        //given
        final var underTest = new KeyOperationsParameters();
        underTest.setValue(input);

        //when
        final var actual = underTest.getValueAsBase64DecodedBytes();

        //then
        Assertions.assertArrayEquals(expected, actual);
    }
}
