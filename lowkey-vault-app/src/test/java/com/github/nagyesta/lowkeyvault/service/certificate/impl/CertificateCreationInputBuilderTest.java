package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ConstantConditions")
class CertificateCreationInputBuilderTest {

    @Test
    void testDnsNamesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.dnsNames(null));

        //then + exception
    }

    @Test
    void testEmailsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.emails(null));

        //then + exception
    }

    @Test
    void testIpsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.upns(null));

        //then + exception
    }

    @Test
    void testKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.keyUsage(null));

        //then + exception
    }

    @Test
    void testExtendedKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.extendedKeyUsage(null));

        //then + exception
    }
}
