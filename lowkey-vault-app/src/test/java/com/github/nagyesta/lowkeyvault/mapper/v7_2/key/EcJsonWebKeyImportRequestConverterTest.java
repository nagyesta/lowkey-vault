package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class EcJsonWebKeyImportRequestConverterTest {

    private EcJsonWebKeyImportRequestConverter underTest;

    public static Stream<Arguments> curveNameProvider() {
        return Arrays.stream(KeyCurveName.values())
                .map(Arguments::of);
    }

    @BeforeEach
    void setUp() {
        underTest = new EcJsonWebKeyImportRequestConverter();
    }

    @ParameterizedTest
    @MethodSource("curveNameProvider")
    void testConvertShouldReturnKeyPairWhenCalledWithValidInput(final KeyCurveName keyCurveName) {
        //given
        final var expected = KeyGenUtil.generateEc(keyCurveName);
        final var expectedPrivate = (BCECPrivateKey) expected.getPrivate();
        final var expectedPublic = (BCECPublicKey) expected.getPublic();
        final var request = new JsonWebKeyImportRequest();
        request.setKeyType(KeyType.EC);
        request.setX(expectedPublic.getQ().getAffineXCoord().getEncoded());
        request.setY(expectedPublic.getQ().getAffineYCoord().getEncoded());
        request.setD(expectedPrivate.getD().toByteArray());
        request.setCurveName(keyCurveName);

        //when
        final var actual = underTest.convert(request);

        //then
        final var actualPrivate = (BCECPrivateKey) actual.getPrivate();
        final var actualPublic = (BCECPublicKey) actual.getPublic();
        Assertions.assertEquals(expectedPrivate.getD(), actualPrivate.getD());
        Assertions.assertEquals(expectedPrivate.getParameters(), actualPrivate.getParameters());
        Assertions.assertEquals(expectedPrivate.getAlgorithm(), actualPrivate.getAlgorithm());
        Assertions.assertEquals(expectedPublic.getQ(), actualPublic.getQ());
        Assertions.assertEquals(expectedPublic.getParameters(), actualPublic.getParameters());
        Assertions.assertEquals(expectedPublic.getAlgorithm(), actualPublic.getAlgorithm());

    }

    @Test
    void testConvertShouldWrapExceptionWhenConversionThrowsOne() {
        //given
        final var source = mock(JsonWebKeyImportRequest.class);
        when(source.getD()).thenThrow(IllegalArgumentException.class);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.convert(source));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("curveNameProvider")
    void testGetKeyParameterShouldReturnTheKeyCurveNameWhenCalledWithValidInput(final KeyCurveName expected) {
        //given
        final var request = mock(JsonWebKeyImportRequest.class);
        when(request.getCurveName()).thenReturn(expected);

        //when
        final var actual = underTest.getKeyParameter(request);

        //then
        Assertions.assertEquals(expected, actual);
        verify(request).getCurveName();
        verifyNoMoreInteractions(request);
    }
}
