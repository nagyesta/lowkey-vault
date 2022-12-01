package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

class EcPrivateKeyToJsonWebKeyImportRequestConverterTest {

    @Test
    void testConvertShouldSetRecognisedCurveNameAndParametersWhenCalledWithValidEcPrivateKey() {
        //given
        final EcPrivateKeyToJsonWebKeyImportRequestConverter underTest = new EcPrivateKeyToJsonWebKeyImportRequestConverter();
        final KeyPair keyPair = KeyGenUtil.generateEc(KeyCurveName.P_256);
        final BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();

        //when
        final JsonWebKeyImportRequest actual = underTest.convert(privateKey);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(KeyType.EC, actual.getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getCurveName());
        Assertions.assertNotNull(actual.getD());
        Assertions.assertNotNull(actual.getX());
        Assertions.assertNotNull(actual.getY());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConvertShouldThrowExceptionWhenCalledWithNull() {
        //given
        final EcPrivateKeyToJsonWebKeyImportRequestConverter underTest = new EcPrivateKeyToJsonWebKeyImportRequestConverter();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(null));

        //then + exceptions
    }
}
