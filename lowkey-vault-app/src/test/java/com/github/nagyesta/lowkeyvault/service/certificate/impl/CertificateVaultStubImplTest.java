package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificateVaultStubImplTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificateVaultStubImpl(null));

        //then + exception
    }
}
