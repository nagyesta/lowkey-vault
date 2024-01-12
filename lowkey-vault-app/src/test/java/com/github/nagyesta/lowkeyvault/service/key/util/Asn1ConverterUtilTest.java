package com.github.nagyesta.lowkeyvault.service.key.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.Stream;

class Asn1ConverterUtilTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validValueProvider() throws NoSuchAlgorithmException {
        final SecureRandom random = SecureRandom.getInstanceStrong();
        return Stream.of(32 * 2, 48 * 2, 66 * 2)
                .map(size -> {
                    final byte[] result = new byte[size];
                    random.nextBytes(result);
                    return result;
                })
                .map(Arguments::of);
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<Asn1ConverterUtil> constructor = Asn1ConverterUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validValueProvider")
    void testConvertAsn1toRawShouldReturnOriginalValuesWhenCalledAfterConvertFromRawToAsn1(final byte[] signatureRaw) throws IOException {
        //given
        final byte[] asn1 = Asn1ConverterUtil.convertFromRawToAsn1(signatureRaw);

        //when
        final byte[] raw = Asn1ConverterUtil.convertFromAsn1toRaw(asn1, signatureRaw.length / 2);

        //then
        Assertions.assertArrayEquals(signatureRaw, raw);
    }
}
