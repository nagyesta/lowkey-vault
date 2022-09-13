package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.SELF_SIGNED;

class IssuerParameterModelTest {

    @Test
    void testControllerShouldSetIssuerWhenCalledWithCertificateAuthority() {
        //given

        //when
        final IssuerParameterModel actual = new IssuerParameterModel(SELF_SIGNED);

        //then
        Assertions.assertEquals(SELF_SIGNED.getValue(), actual.getIssuer());
    }

    @Test
    void testControllerShouldNotSetIssuerWhenCalledWithoutCertificateAuthority() {
        //given

        //when
        final IssuerParameterModel actual = new IssuerParameterModel(null);

        //then
        Assertions.assertNull(actual.getIssuer());
    }
}
