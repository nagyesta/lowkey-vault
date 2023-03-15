package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.TestConstantsCertificates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultCertificateLifetimeActionPolicyTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullIssuer() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DefaultCertificateLifetimeActionPolicy(
                TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_1, null));

        //then + exception
    }
}
