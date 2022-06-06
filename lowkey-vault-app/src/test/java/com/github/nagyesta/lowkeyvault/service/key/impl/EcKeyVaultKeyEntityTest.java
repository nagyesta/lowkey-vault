package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.Mockito.mock;

class EcKeyVaultKeyEntityTest {

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_384 = "SHA-384";
    private static final String SHA_512 = "SHA-512";
    private static final Map<SignatureAlgorithm, String> HASH_ALGORITHMS = Map.of(
            SignatureAlgorithm.ES256, SHA_256,
            SignatureAlgorithm.ES256K, SHA_256,
            SignatureAlgorithm.ES384, SHA_384,
            SignatureAlgorithm.ES512, SHA_512);

    public static Stream<Arguments> invalidValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, null))
                .add(Arguments.of(null, mock(VaultFake.class), null))
                .add(Arguments.of(null, null, KeyCurveName.P_256))
                .add(Arguments.of(null, mock(VaultFake.class), KeyCurveName.P_256))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, KeyCurveName.P_256))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, mock(VaultFake.class), null))
                .build();
    }

    public static Stream<Arguments> stringSignSource() {
        return Arrays.stream(SignatureAlgorithm.values())
                .filter(sa -> sa.isCompatible(KeyType.EC))
                .flatMap(sa -> Arrays.stream(KeyCurveName.values())
                        .filter(sa::isCompatibleWithCurve)
                        .flatMap(kcn -> Stream.<Arguments>builder()
                                .add(Arguments.of(DEFAULT_VAULT, DEFAULT_VAULT, sa, kcn))
                                .add(Arguments.of(LOCALHOST, LOCALHOST, sa, kcn))
                                .add(Arguments.of(LOWKEY_VAULT, LOWKEY_VAULT, sa, kcn))
                                .add(Arguments.of(DEFAULT_VAULT, LOCALHOST, sa, kcn))
                                .add(Arguments.of(LOCALHOST, DEFAULT_VAULT, sa, kcn))
                                .add(Arguments.of(LOWKEY_VAULT, LOCALHOST, sa, kcn))
                                .build()));
    }

    public static Stream<Arguments> digestSource() {
        final Object bytes = DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8);
        return Stream.<Arguments>builder()
                .add(Arguments.of(bytes))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidValueProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedKeyEntityId id, final VaultFake vault, final KeyCurveName keyParam) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EcKeyVaultKeyEntity(id, vault, keyParam, false));

        //then + exception
    }

    @Test
    void testEncryptThenDecryptShouldBothThrowExceptionsWhenCalled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_521, false);

        //when
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, null, null));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> underTest.decrypt(null, null, null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("stringSignSource")
    void testSignThenVerifyShouldReturnTrueWhenVerificationAndSignAreUsingTheSameData(
            final String clearSign, final String clearVerify, final SignatureAlgorithm algorithm, final KeyCurveName keyCurveName) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyCurveName, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] cipherSign = hash(clearSign.getBytes(StandardCharsets.UTF_8), algorithm);
        final byte[] cipherVerify = hash(clearVerify.getBytes(StandardCharsets.UTF_8), algorithm);

        //when
        final byte[] signature = underTest.signBytes(cipherSign, algorithm);
        final boolean actual = underTest.verifySignedBytes(cipherVerify, algorithm, signature);

        //then
        Assertions.assertEquals(clearSign.equals(clearVerify), actual);
    }

    @Test
    void testSignShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.signBytes(digest, SignatureAlgorithm.ES256));

        //then + exception
    }

    @Test
    void testVerifyShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN));
        underTest.setEnabled(true);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        final byte[] signature = underTest.signBytes(digest, SignatureAlgorithm.ES256);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(digest, SignatureAlgorithm.ES256, signature));

        //then + exception
    }

    @Test
    void testSignShouldThrowExceptionWhenOperationIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(false);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.signBytes(digest, SignatureAlgorithm.ES256));

        //then + exception
    }

    @Test
    void testVerifyShouldThrowExceptionWhenKeyIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        final byte[] signature = underTest.signBytes(digest, SignatureAlgorithm.ES256);
        underTest.setEnabled(false);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(digest, SignatureAlgorithm.ES256, signature));

        //then + exception
    }

    @Test
    void testSignShouldThrowExceptionWhenKeyCurveIsNotCompatible() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.signBytes(digest, SignatureAlgorithm.ES256K));

        //then + exception
    }

    @Test
    void testVerifyShouldThrowExceptionWhenWhenKeyCurveIsNotCompatible() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] digest = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        final byte[] signature = underTest.signBytes(digest, SignatureAlgorithm.ES256);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(digest, SignatureAlgorithm.ES256K, signature));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("digestSource")
    @NullSource
    void testSignShouldThrowExceptionWhenWhenDigestSizeIsNotCompatible(final byte[] digest) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.signBytes(digest, SignatureAlgorithm.ES256));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("digestSource")
    @NullSource
    void testVerifyShouldThrowExceptionWhenWhenDigestSizeIsNotCompatible(final byte[] digest) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] hash = hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256);

        //when
        final byte[] signature = underTest.signBytes(hash, SignatureAlgorithm.ES256);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.verifySignedBytes(digest, SignatureAlgorithm.ES256, signature));

        //then + exception
    }

    @Test
    void testKeyCreationInputShouldReturnOriginalParameters() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final KeyCurveName keyCurveName = KeyCurveName.P_384;
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyCurveName, false);

        //when
        final KeyCreationInput<?> actual = underTest.keyCreationInput();

        //then
        Assertions.assertInstanceOf(EcKeyCreationInput.class, actual);
        final EcKeyCreationInput value = (EcKeyCreationInput) actual;
        Assertions.assertEquals(keyCurveName, value.getKeyParameter());
    }

    private byte[] hash(final byte[] text, final SignatureAlgorithm algorithm) {
        try {
            final MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHMS.get(algorithm));
            md.update(text);
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
