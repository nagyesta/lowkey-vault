package com.github.nagyesta.lowkeyvault.service.key.util;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.SecretKey;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_AES_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;

class KeyGenUtilTest {

    public static Stream<Arguments> symmetricProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, MIN_AES_KEY_SIZE))
                .add(Arguments.of(KeyType.OCT_HSM.getAlgorithmName(), 1))
                .build();
    }

    public static Stream<Arguments> asymmetricProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, MIN_RSA_KEY_SIZE))
                .add(Arguments.of(KeyType.RSA.getAlgorithmName(), 0))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> randomByteCountProvider() {
        return IntStream.of(-5, -1, 0, 1, 2, 5, 10, 42, 200)
                .mapToObj(Arguments::of);
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<KeyGenUtil> constructor = KeyGenUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("symmetricProvider")
    void testKeyGeneratorShouldCatchAndWrapExceptionsWhenTheyAreThrown(final String alg, final int size) {
        //given

        //when
        Assertions.assertThrows(CryptoException.class, () -> KeyGenUtil.keyGenerator(alg, size));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("asymmetricProvider")
    void testKeyPairGeneratorShouldCatchAndWrapExceptionsWhenTheyAreThrown(final String alg, final Integer size) {
        //given
        final RSAKeyGenParameterSpec rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(size, BigInteger.ONE);

        //when
        Assertions.assertThrows(CryptoException.class, () -> KeyGenUtil.keyPairGenerator(alg, rsaKeyGenParameterSpec));

        //then + exception
    }

    @Test
    void testGenerateRsaGeneratesAnRsaKeyPairWhenCalledWithValidInput() {
        //given

        //when
        final KeyPair actual = KeyGenUtil.generateRsa(MIN_RSA_KEY_SIZE, null);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertInstanceOf(RSAPrivateKey.class, actual.getPrivate());
        Assertions.assertInstanceOf(RSAPublicKey.class, actual.getPublic());
    }

    @Test
    void testGenerateEcGeneratesAnEcKeyPairWhenCalledWithValidInput() {
        //given

        //when
        final KeyPair actual = KeyGenUtil.generateEc(KeyCurveName.P_256);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertInstanceOf(ECPrivateKey.class, actual.getPrivate());
        Assertions.assertInstanceOf(ECPublicKey.class, actual.getPublic());
    }

    @Test
    void testGenerateAesGeneratesAnAesKeyWhenCalledWithValidInput() {
        //given

        //when
        final SecretKey actual = KeyGenUtil.generateAes(MIN_AES_KEY_SIZE);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(KeyType.OCT_HSM.getAlgorithmName(), actual.getAlgorithm());
    }

    @ParameterizedTest
    @MethodSource("randomByteCountProvider")
    void testGenerateRandomBytesShouldReturnTheRightAmountOfRandomBytesWhenCalledWithPositiveNumber(final int number) {
        //given

        //when
        if (number <= 0) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> KeyGenUtil.generateRandomBytes(number));
        } else {
            final byte[] actual = Assertions.assertDoesNotThrow(() -> KeyGenUtil.generateRandomBytes(number));

            //then
            Assertions.assertEquals(number, actual.length);
        }
    }
}
