package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.github.nagyesta.lowkeyvault.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class SignatureAlgorithmTest {

    public static Stream<Arguments> valueProvider() {
        final Stream.Builder<Arguments> builder = Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(TestConstants.EMPTY, null));
        Arrays.stream(SignatureAlgorithm.values()).forEach(a -> builder.add(Arguments.of(a.getValue(), a)));
        return builder.build();
    }

    @Test
    void testIsCompatibleWithCurveShouldReturnFalseInCaseOfRsaAlgorithm() {
        //given
        final SignatureAlgorithm underTest = SignatureAlgorithm.PS256;

        //when
        final boolean actual = underTest.isCompatibleWithCurve(KeyCurveName.P_256);

        //then
        Assertions.assertFalse(actual);
    }


    @Test
    void testSupportsDigestLengthShouldReturnFalseInCaseOfRsaAlgorithm() {
        //given
        final SignatureAlgorithm underTest = SignatureAlgorithm.PS256;

        //when
        final boolean actual = underTest.supportsDigestLength(0);

        //then
        Assertions.assertFalse(actual);
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void forValue(final String inout, final SignatureAlgorithm expected) {
        //given

        //when
        final SignatureAlgorithm actual = SignatureAlgorithm.forValue(inout);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
