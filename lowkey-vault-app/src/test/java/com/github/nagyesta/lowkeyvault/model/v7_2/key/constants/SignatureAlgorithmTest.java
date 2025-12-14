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
        final var builder = Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(TestConstants.EMPTY, null));
        Arrays.stream(SignatureAlgorithm.values()).forEach(a -> builder.add(Arguments.of(a.getValue(), a)));
        return builder.build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> digestLengthProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(SignatureAlgorithm.ES256, 32, true))
                .add(Arguments.of(SignatureAlgorithm.ES256, 31, false))
                .add(Arguments.of(SignatureAlgorithm.ES256, 33, false))
                .add(Arguments.of(SignatureAlgorithm.ES256, 16, false))
                .add(Arguments.of(SignatureAlgorithm.ES256, 64, false))
                .add(Arguments.of(SignatureAlgorithm.ES256K, 32, true))
                .add(Arguments.of(SignatureAlgorithm.ES256K, 31, false))
                .add(Arguments.of(SignatureAlgorithm.ES256K, 33, false))
                .add(Arguments.of(SignatureAlgorithm.ES256K, 16, false))
                .add(Arguments.of(SignatureAlgorithm.ES256K, 64, false))
                .add(Arguments.of(SignatureAlgorithm.ES384, 48, true))
                .add(Arguments.of(SignatureAlgorithm.ES384, 47, false))
                .add(Arguments.of(SignatureAlgorithm.ES384, 49, false))
                .add(Arguments.of(SignatureAlgorithm.ES384, 32, false))
                .add(Arguments.of(SignatureAlgorithm.ES384, 64, false))
                .add(Arguments.of(SignatureAlgorithm.ES512, 64, true))
                .add(Arguments.of(SignatureAlgorithm.ES512, 63, false))
                .add(Arguments.of(SignatureAlgorithm.ES512, 65, false))
                .add(Arguments.of(SignatureAlgorithm.ES512, 32, false))
                .add(Arguments.of(SignatureAlgorithm.ES512, 128, false))
                .build();
    }

    @Test
    void testIsCompatibleWithCurveShouldReturnFalseInCaseOfRsaAlgorithm() {
        //given
        final var underTest = SignatureAlgorithm.PS256;

        //when
        final var actual = underTest.isCompatibleWithCurve(KeyCurveName.P_256);

        //then
        Assertions.assertFalse(actual);
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testForValueShouldReturnExpectedAlgorithmWhenCalled(
            final String inout,
            final SignatureAlgorithm expected) {
        //given

        //when
        final var actual = SignatureAlgorithm.forValue(inout);

        //then
        Assertions.assertEquals(expected, actual);
    }
}
