package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;

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
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setDnsNames(null));

        //then + exception
    }

    @Test
    void testSetDnsNamesShouldSetValueWhenCalledWithValidInput() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());
        final var expected = Set.of(LOCALHOST);

        //when
        underTest.setDnsNames(expected);

        //then
        Assertions.assertIterableEquals(expected, underTest.getDnsNames());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetUpnsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setUpns(null));

        //then + exception
    }

    @Test
    void testSetUpnsShouldSetValueWhenCalledWithValidInput() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());
        final var expected = Set.of("value");

        //when
        underTest.setUpns(expected);

        //then
        Assertions.assertIterableEquals(expected, underTest.getUpns());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetEmailsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setEmails(null));

        //then + exception
    }

    @Test
    void testSetEmailsShouldSetValueWhenCalledWithValidInput() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());
        final var expected = Set.of("someone@example.com");

        //when
        underTest.setEmails(expected);

        //then
        Assertions.assertIterableEquals(expected, underTest.getEmails());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setKeyUsage(null));

        //then + exception
    }

    @Test
    void testSetKeyUsageShouldSetValueWhenCalledWithValidInput() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());
        final var expected = Set.of(KeyUsageEnum.DIGITAL_SIGNATURE);

        //when
        underTest.setKeyUsage(expected);

        //then
        Assertions.assertIterableEquals(expected, underTest.getKeyUsage());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetExtendedKeyUsageShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setExtendedKeyUsage(null));

        //then + exception
    }

    @Test
    void testSetExtendedKeyUsageShouldSetValueWhenCalledWithValidInput() {
        //given
        final var underTest = new CertificatePolicy(CertificateCreationInput.builder().build());
        final var expected = Set.of("1.3.6.1.5.5.7.3.2");

        //when
        underTest.setExtendedKeyUsage(expected);

        //then
        Assertions.assertIterableEquals(expected, underTest.getExtendedKeyUsage());
    }
}
