package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.HashUtil;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.*;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.Mockito.mock;

class EcKeyVaultKeyEntityTest {

    public static Stream<Arguments> invalidValueProvider() {
        return Stream.<Arguments>builder()
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

    public static Stream<Arguments> byteArraySource() {
        final Object bytes = DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8);
        return Stream.<Arguments>builder()
                .add(Arguments.of(bytes))
                .build();
    }

    public static Stream<Arguments> signDigestSource() {
        final byte[] bytes = DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8);
        final byte[] sha256Digest = HashUtil.hash(bytes, HashAlgorithm.SHA256);
        final byte[] sha384Digest = HashUtil.hash(bytes, HashAlgorithm.SHA384);
        final byte[] sha512Digest = HashUtil.hash(bytes, HashAlgorithm.SHA512);
        return Stream.<Arguments>builder()
                .add(Arguments.of(bytes, sha256Digest, KeyCurveName.P_256, SignatureAlgorithm.ES256, "SHA256withPLAIN-ECDSA"))
                .add(Arguments.of(bytes, sha256Digest, KeyCurveName.P_256K, SignatureAlgorithm.ES256K, "SHA256withPLAIN-ECDSA"))
                .add(Arguments.of(bytes, sha384Digest, KeyCurveName.P_384, SignatureAlgorithm.ES384, "SHA384withPLAIN-ECDSA"))
                .add(Arguments.of(bytes, sha512Digest, KeyCurveName.P_521, SignatureAlgorithm.ES512, "SHA512withPLAIN-ECDSA"))
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
        final byte[] cipherSign = HashUtil.hash(clearSign.getBytes(StandardCharsets.UTF_8), algorithm.getHashAlgorithm());
        final byte[] cipherVerify = HashUtil.hash(clearVerify.getBytes(StandardCharsets.UTF_8), algorithm.getHashAlgorithm());

        //when
        final byte[] signature = underTest.signBytes(cipherSign, algorithm);
        final boolean actual = underTest.verifySignedBytes(cipherVerify, algorithm, signature);

        //then
        Assertions.assertEquals(clearSign.equals(clearVerify), actual);
    }

    @ParameterizedTest
    @MethodSource("signDigestSource")
    void testSignShouldProduceTheExpectedSignatureWhenCalledWithValidData(
            final byte[] original, final byte[] digest, final KeyCurveName keyCurveName,
            final SignatureAlgorithm algorithm, final String verifyAlgorithm) throws Exception {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyCurveName, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);

        //when
        final byte[] signature = underTest.signBytes(digest, algorithm);

        //then
        final boolean actual = checkSignature(underTest.getKey().getPublic(), signature, original, verifyAlgorithm);
        Assertions.assertTrue(actual);
    }

    @Test
    void testSignShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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
        final byte[] digest = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

        //when
        final byte[] signature = underTest.signBytes(digest, SignatureAlgorithm.ES256);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(digest, SignatureAlgorithm.ES256K, signature));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("byteArraySource")
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
    @MethodSource("byteArraySource")
    @NullSource
    void testVerifyShouldThrowExceptionWhenWhenDigestSizeIsNotCompatible(final byte[] digest) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final EcKeyVaultKeyEntity underTest = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyCurveName.P_256, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);
        final byte[] hash = HashUtil.hash(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.ES256.getHashAlgorithm());

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

    private boolean checkSignature(
            final PublicKey publicKey, final byte[] signatureToCheck,
            final byte[] originalDigest, final String algName) throws Exception {
        final Signature sig = Signature.getInstance(algName, new BouncyCastleProvider());
        sig.initVerify(publicKey);
        sig.update(originalDigest);
        return sig.verify(signatureToCheck);
    }
}
