package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.CertificateCreationInputBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ConstantConditions")
class CertificateCreationInputBuilderTest {

    @Test
    void testDnsNamesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateCreationInputBuilder underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.dnsNames(null));

        //then + exception
    }

    @Test
    void testEmailsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateCreationInputBuilder underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.emails(null));

        //then + exception
    }

    @Test
    void testIpsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateCreationInputBuilder underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.ips(null));

        //then + exception
    }

    @Test
    void testKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateCreationInputBuilder underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.keyUsage(null));

        //then + exception
    }

    @Test
    void testExtendedKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateCreationInputBuilder underTest = CertificateCreationInput.builder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.extendedKeyUsage(null));

        //then + exception
    }
}
