package com.github.nagyesta.lowkeyvault.service.key.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KeyCreationInputTest {

    @Test
    void testConstructorShouldThrowExceptionsWhenCalledWithNullKeyType() {
        //given

        //when
        //noinspection ConstantConditions
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyCreationInput<>(null, null));

        //then exception
    }
}
