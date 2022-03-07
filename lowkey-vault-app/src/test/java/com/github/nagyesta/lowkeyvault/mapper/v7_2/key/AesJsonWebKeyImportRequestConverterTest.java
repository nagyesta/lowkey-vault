package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class AesJsonWebKeyImportRequestConverterTest {

    private static final int AES_128 = 128;
    private static final int AES_192 = 192;
    private static final int AES_256 = 256;
    private AesJsonWebKeyImportRequestConverter underTest;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> sizeProvider() {
        return IntStream.of(AES_128, AES_192, AES_256)
                .mapToObj(bits -> Arguments.of(bits / 8, bits));
    }

    @BeforeEach
    void setUp() {
        underTest = new AesJsonWebKeyImportRequestConverter();
    }

    @ParameterizedTest
    @ValueSource(ints = {AES_128, AES_192, AES_256})
    void testConvertShouldReturnSecretKeyWhenCalledWithValidInput(final int size) {
        //given
        final SecretKey expected = KeyGenUtil.generateAes(size);
        final JsonWebKeyImportRequest request = new JsonWebKeyImportRequest();
        request.setKeyType(KeyType.OCT_HSM);
        request.setK(expected.getEncoded());

        //when
        final SecretKey actual = underTest.convert(request);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testConvertShouldWrapExceptionWhenConversionThrowsOne() {
        //given
        final JsonWebKeyImportRequest source = mock(JsonWebKeyImportRequest.class);
        when(source.getK()).thenThrow(IllegalArgumentException.class);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.convert(source));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("sizeProvider")
    void testGetKeyParameterShouldReturnTheKeySizeWhenCalledWithValidInput(final int sizeInBytes, final int expected) {
        //given
        final JsonWebKeyImportRequest request = mock(JsonWebKeyImportRequest.class);
        when(request.getK()).thenReturn(new byte[sizeInBytes]);

        //when
        final Integer actual = underTest.getKeyParameter(request);

        //then
        Assertions.assertEquals(expected, actual);
        verify(request).getK();
        verifyNoMoreInteractions(request);
    }
}
