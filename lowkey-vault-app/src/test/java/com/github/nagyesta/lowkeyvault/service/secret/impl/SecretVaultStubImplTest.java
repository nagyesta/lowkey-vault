package com.github.nagyesta.lowkeyvault.service.secret.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SecretVaultStubImplTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SecretVaultStubImpl(null));

        //then + exception
    }
}
