package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class KeyUsageEnumTest {

    @Test
    void testCombiningItemsShouldCreateSingleKeyUsageWhenCalledForValidSetOfUsages() {
        //given
        final KeyUsageEnum[] values = KeyUsageEnum.values();

        //when
        final KeyUsage actual = Stream.of(values).collect(KeyUsageEnum.toKeyUsage());

        //then
        Stream.of(values).forEach(value -> {
            Assertions.assertTrue(actual.hasUsages(value.getCode()), "Usage must be present: " + value.getValue());
        });
    }
}
