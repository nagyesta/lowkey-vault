package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;

class RsaPrivateKeyToJsonWebKeyImportRequestConverterTest {

    @Test
    void testConvertShouldSetParametersWhenCalledWithValidRsaPrivateKey() {
        //given
        final RsaPrivateKeyToJsonWebKeyImportRequestConverter underTest = new RsaPrivateKeyToJsonWebKeyImportRequestConverter();
        final KeyPair keyPair = KeyGenUtil.generateRsa(2048, null);
        final RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();

        //when
        final JsonWebKeyImportRequest actual = underTest.convert(privateKey);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(KeyType.RSA, actual.getKeyType());
        Assertions.assertNotNull(actual.getN());
        Assertions.assertNotNull(actual.getE());
        Assertions.assertNotNull(actual.getD());
        Assertions.assertNotNull(actual.getP());
        Assertions.assertNotNull(actual.getQ());
        Assertions.assertNotNull(actual.getDp());
        Assertions.assertNotNull(actual.getDq());
        Assertions.assertNotNull(actual.getQi());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConvertShouldThrowExceptionWhenCalledWithNull() {
        //given
        final RsaPrivateKeyToJsonWebKeyImportRequestConverter underTest = new RsaPrivateKeyToJsonWebKeyImportRequestConverter();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(null));

        //then + exceptions
    }
}
