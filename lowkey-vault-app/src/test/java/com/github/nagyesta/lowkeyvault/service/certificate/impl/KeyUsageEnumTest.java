package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum.*;

class KeyUsageEnumTest {

    public static Stream<Arguments> bitStringProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(new boolean[]{true, true, true, true, true, true, true, true, true},
                        Set.of(DIGITAL_SIGNATURE, NON_REPUDIATION, KEY_ENCIPHERMENT, DATA_ENCIPHERMENT, KEY_AGREEMENT,
                                KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, true, true, true, true, true, true, true, true},
                        Set.of(NON_REPUDIATION, KEY_ENCIPHERMENT, DATA_ENCIPHERMENT, KEY_AGREEMENT,
                                KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, true, true, true, true, true, true, true},
                        Set.of(KEY_ENCIPHERMENT, DATA_ENCIPHERMENT, KEY_AGREEMENT,
                                KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, true, true, true, true, true, true},
                        Set.of(DATA_ENCIPHERMENT, KEY_AGREEMENT, KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, true, true, true, true, true},
                        Set.of(KEY_AGREEMENT, KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, false, true, true, true, true},
                        Set.of(KEY_CERT_SIGN, CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, false, false, true, true, true},
                        Set.of(CRL_SIGN, ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, false, false, false, true, true},
                        Set.of(ENCIPHER_ONLY, DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, false, false, false, false, true},
                        Set.of(DECIPHER_ONLY)))
                .add(Arguments.of(new boolean[]{false, false, false, false, false, false, false, false, false},
                        Set.of()))
                .add(Arguments.of(new boolean[]{},
                        Set.of()))
                .add(Arguments.of(null,
                        Set.of()))
                .add(Arguments.of(new boolean[]{true, false, true, true},
                        Set.of(DIGITAL_SIGNATURE, KEY_ENCIPHERMENT, DATA_ENCIPHERMENT)))

                .build();
    }

    public static Stream<Arguments> byValueProvider() {
        return Arrays.stream(KeyUsageEnum.values())
                .map(e -> Arguments.of(e.getValue(), e));
    }

    @Test
    void testCombiningItemsShouldCreateSingleKeyUsageWhenCalledForValidSetOfUsages() {
        //given
        final KeyUsageEnum[] values = KeyUsageEnum.values();

        //when
        final KeyUsage actual = Stream.of(values).collect(KeyUsageEnum.toKeyUsage());

        //then
        Stream.of(values).forEach(value -> {
            Assertions.assertTrue(actual.hasUsages(value.getCode()), "Usage must be present: " + value.getValue());
        });
    }

    @ParameterizedTest
    @MethodSource("bitStringProvider")
    void testParseBitStringShouldFindSelectedEnumsWhenCalledWithValidData(final boolean[] input, final Set<KeyUsageEnum> expected) {
        //given

        //when
        final Set<KeyUsageEnum> actual = KeyUsageEnum.parseBitString(input);

        //then
        Assertions.assertIterableEquals(new TreeSet<>(expected), new TreeSet<>(actual));
    }

    @ParameterizedTest
    @MethodSource("byValueProvider")
    void testByValueShouldFindSelectedEnumWhenCalledWithValidData(final String input, final KeyUsageEnum expected) {
        //given

        //when
        final KeyUsageEnum actual = KeyUsageEnum.byValue(input);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testByValueShouldThrowExceptionWhenCalledWithInvalidData(final String input) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> KeyUsageEnum.byValue(input));

        //then + exception
    }
}
