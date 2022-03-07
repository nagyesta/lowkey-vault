package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class RsaJsonWebKeyImportRequestConverterTest {

    private static final int RSA_2048 = 2048;
    private static final int RSA_3072 = 3072;
    private static final int RSA_4096 = 4096;
    private RsaJsonWebKeyImportRequestConverter underTest;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> sizeProvider() {
        return IntStream.of(RSA_2048, RSA_3072, RSA_4096)
                .mapToObj(bits -> Arguments.of(bits / 8, bits));
    }

    @BeforeEach
    void setUp() {
        underTest = new RsaJsonWebKeyImportRequestConverter();
    }

    @ParameterizedTest
    @ValueSource(ints = {RSA_2048, RSA_3072, RSA_4096})
    void testConvertShouldReturnKeyPairWhenCalledWithValidInput(final int size) {
        //given
        final KeyPair expected = KeyGenUtil.generateRsa(size, null);
        final JsonWebKeyImportRequest request = new JsonWebKeyImportRequest();
        request.setKeyType(KeyType.RSA);
        final BCRSAPublicKey expectedPublic = (BCRSAPublicKey) expected.getPublic();
        request.setN(expectedPublic.getModulus().toByteArray());
        request.setE(expectedPublic.getPublicExponent().toByteArray());
        final RSAPrivateCrtKey expectedPrivateKey = (RSAPrivateCrtKey) expected.getPrivate();
        request.setD(expectedPrivateKey.getPrivateExponent().toByteArray());
        request.setDp(expectedPrivateKey.getPrimeExponentP().toByteArray());
        request.setDq(expectedPrivateKey.getPrimeExponentQ().toByteArray());
        request.setP(expectedPrivateKey.getPrimeP().toByteArray());
        request.setQ(expectedPrivateKey.getPrimeQ().toByteArray());
        request.setQi(expectedPrivateKey.getCrtCoefficient().toByteArray());

        //when
        final KeyPair actual = underTest.convert(request);

        //then
        Assertions.assertEquals(expected.getPrivate(), actual.getPrivate());
        Assertions.assertEquals(expected.getPublic(), actual.getPublic());
    }

    @Test
    void testConvertShouldWrapExceptionWhenConversionThrowsOne() {
        //given
        final JsonWebKeyImportRequest source = mock(JsonWebKeyImportRequest.class);
        when(source.getD()).thenThrow(IllegalArgumentException.class);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.convert(source));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("sizeProvider")
    void testGetKeyParameterShouldReturnTheKeySizeWhenCalledWithValidInput(final int sizeInBytes, final int expected) {
        //given
        final JsonWebKeyImportRequest request = mock(JsonWebKeyImportRequest.class);
        when(request.getN()).thenReturn(new byte[sizeInBytes]);

        //when
        final Integer actual = underTest.getKeyParameter(request);

        //then
        Assertions.assertEquals(expected, actual);
        verify(request).getN();
        verifyNoMoreInteractions(request);
    }
}
