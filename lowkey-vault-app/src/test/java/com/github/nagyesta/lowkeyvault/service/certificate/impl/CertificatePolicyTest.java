package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificatePolicyTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificatePolicy(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetDnsNamesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificatePolicy underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setDnsNames(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetIpsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificatePolicy underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setIps(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetEmailsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificatePolicy underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setEmails(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificatePolicy underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setKeyUsage(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetExtendedKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificatePolicy underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setExtendedKeyUsage(null));

        //then + exception
    }
}
