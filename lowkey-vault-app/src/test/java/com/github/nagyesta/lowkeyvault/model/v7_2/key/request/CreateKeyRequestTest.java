package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.OctKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyCreationInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_AES_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;

class CreateKeyRequestTest {

    public static Stream<Arguments> rsaInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.RSA, MIN_RSA_KEY_SIZE, null))
                .add(Arguments.of(KeyType.RSA_HSM, MIN_RSA_KEY_SIZE, null))
                .add(Arguments.of(KeyType.RSA, MIN_RSA_KEY_SIZE, BigInteger.TEN))
                .add(Arguments.of(KeyType.RSA_HSM, MIN_RSA_KEY_SIZE, BigInteger.TEN))
                .add(Arguments.of(KeyType.RSA, null, null))
                .add(Arguments.of(KeyType.RSA_HSM, null, null))
                .add(Arguments.of(KeyType.RSA, null, BigInteger.TWO))
                .add(Arguments.of(KeyType.RSA_HSM, null, BigInteger.ONE))
                .build();
    }

    public static Stream<Arguments> ecInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.EC, KeyCurveName.P_256))
                .add(Arguments.of(KeyType.EC_HSM, KeyCurveName.P_256))
                .add(Arguments.of(KeyType.EC, KeyCurveName.P_256K))
                .add(Arguments.of(KeyType.EC_HSM, KeyCurveName.P_256K))
                .add(Arguments.of(KeyType.EC, null))
                .add(Arguments.of(KeyType.EC_HSM, null))
                .build();
    }

    public static Stream<Arguments> octInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.OCT_HSM, MIN_AES_KEY_SIZE))
                .add(Arguments.of(KeyType.OCT_HSM, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("octInputProvider")
    void testToKeyCreationInputShouldReturnKeySizeWhenTypeIsOct(
            final KeyType keyType,
            final Integer parameter) {
        //given
        final var underTest = new CreateKeyRequest();
        underTest.setKeyType(keyType);
        underTest.setKeySize(parameter);
        final var expected = new OctKeyCreationInput(keyType, parameter);

        //when
        final var actual = underTest.toKeyCreationInput();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("rsaInputProvider")
    void testToKeyCreationInputShouldReturnKeySizeWhenTypeIsRsa(
            final KeyType keyType,
            final Integer parameter,
            final BigInteger publicExponent) {
        //given
        final var underTest = new CreateKeyRequest();
        underTest.setKeyType(keyType);
        underTest.setKeySize(parameter);
        underTest.setPublicExponent(publicExponent);
        final var expected = new RsaKeyCreationInput(keyType, parameter, publicExponent);

        //when
        final var actual = underTest.toKeyCreationInput();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("ecInputProvider")
    void testToKeyCreationInputShouldReturnCurveNameWhenTypeIsEc(
            final KeyType keyType,
            final KeyCurveName parameter) {
        //given
        final var underTest = new CreateKeyRequest();
        underTest.setKeyType(keyType);
        underTest.setKeyCurveName(parameter);
        final var keyCurveName = Objects.requireNonNullElse(parameter, KeyCurveName.P_256);
        final var expected = new EcKeyCreationInput(keyType, keyCurveName);

        //when
        final var actual = underTest.toKeyCreationInput();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testSetPublicExponentShouldSetNullWhenCalledWithZero() {
        //given
        final var underTest = new CreateKeyRequest();

        //when
        underTest.setPublicExponent(BigInteger.ZERO);

        //then
        Assertions.assertNull(underTest.getPublicExponent());
    }
}
