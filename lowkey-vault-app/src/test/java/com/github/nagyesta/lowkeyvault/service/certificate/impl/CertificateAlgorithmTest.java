package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CertificateAlgorithmTest {

    public static Stream<Arguments> invalidKeyTypeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.OCT))
                .add(Arguments.of(KeyType.OCT_HSM))
                .build();
    }

    public static Stream<Arguments> validKeyTypeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.EC, CertificateAlgorithm.EC, "SHA256withECDSA"))
                .add(Arguments.of(KeyType.EC_HSM, CertificateAlgorithm.EC, "SHA256withECDSA"))
                .add(Arguments.of(KeyType.RSA, CertificateAlgorithm.RSA, "SHA256withRSA"))
                .add(Arguments.of(KeyType.RSA_HSM, CertificateAlgorithm.RSA, "SHA256withRSA"))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidKeyTypeProvider")
    void testForKeyTypeShouldThrowExceptionWhenCalledWithNullOrUnknownKeyType(final KeyType keyType) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> CertificateAlgorithm.forKeyType(keyType));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validKeyTypeProvider")
    void testForKeyTypeShouldReturnExpectedAlgorithmWhenCalledWithKnownKeyType(
            final KeyType keyType,
            final CertificateAlgorithm expectedInstance,
            final String expectedAlgorithm) {
        //given

        //when
        final var actualInstance = CertificateAlgorithm.forKeyType(keyType);
        final var actualAlgorithm = actualInstance.getAlgorithm();

        //then
        Assertions.assertEquals(expectedInstance, actualInstance);
        Assertions.assertEquals(expectedAlgorithm, actualAlgorithm);
    }
}
