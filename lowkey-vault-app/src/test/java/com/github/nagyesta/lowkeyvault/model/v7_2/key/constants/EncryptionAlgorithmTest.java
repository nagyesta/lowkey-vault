package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.github.nagyesta.lowkeyvault.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class EncryptionAlgorithmTest {

    public static Stream<Arguments> valueProvider() {
        final Stream.Builder<Arguments> builder = Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(TestConstants.EMPTY, null));
        Arrays.stream(EncryptionAlgorithm.values()).forEach(a -> builder.add(Arguments.of(a.getValue(), a)));
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void forValue(final String inout, final EncryptionAlgorithm expected) {
        //given

        //when
        final EncryptionAlgorithm actual = EncryptionAlgorithm.forValue(inout);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
