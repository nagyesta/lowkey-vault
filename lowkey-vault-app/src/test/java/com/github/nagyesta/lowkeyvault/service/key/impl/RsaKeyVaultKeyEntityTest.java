package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.Mockito.mock;

class RsaKeyVaultKeyEntityTest {

    public static Stream<Arguments> invalidValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, null, null))
                .add(Arguments.of(null, mock(VaultFake.class), null, null))
                .add(Arguments.of(null, null, MIN_RSA_KEY_SIZE, null))
                .add(Arguments.of(null, mock(VaultFake.class), MIN_RSA_KEY_SIZE, null))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, MIN_RSA_KEY_SIZE, null))
                .build();
    }

    public static Stream<Arguments> stringSource() {
        return Arrays.stream(EncryptionAlgorithm.values())
                .filter(ea -> ea.isCompatible(KeyType.RSA))
                .flatMap(ea -> Stream.<Arguments>builder()
                        .add(Arguments.of(DEFAULT_VAULT, ea))
                        .add(Arguments.of(LOCALHOST, ea))
                        .add(Arguments.of(LOWKEY_VAULT, ea))
                        .build());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> stringSignSource() {
        return Arrays.stream(SignatureAlgorithm.values())
                .filter(sa -> sa.isCompatible(KeyType.RSA))
                .flatMap(sa -> Stream.<Arguments>builder()
                        .add(Arguments.of(DEFAULT_VAULT, DEFAULT_VAULT, sa, 2048))
                        .add(Arguments.of(LOCALHOST, LOCALHOST, sa, 3072))
                        .add(Arguments.of(LOWKEY_VAULT, LOWKEY_VAULT, sa, 4096))
                        .add(Arguments.of(DEFAULT_VAULT, LOCALHOST, sa, 2048))
                        .add(Arguments.of(LOCALHOST, DEFAULT_VAULT, sa, 3072))
                        .add(Arguments.of(LOWKEY_VAULT, LOCALHOST, sa, 4096))
                        .build());
    }

    @ParameterizedTest
    @MethodSource("invalidValueProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedKeyEntityId id, final VaultFake vault, final Integer keySize, final BigInteger exponent) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RsaKeyVaultKeyEntity(id, vault, keySize, exponent, false));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("stringSource")
    void testEncryptThenDecryptShouldReturnOriginalTextWhenCalled(final String clear, final EncryptionAlgorithm algorithm) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, algorithm.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);
        Assertions.assertEquals(algorithm.getMinKeySize(), underTest.getKeySize());

        //when
        final byte[] encrypted = underTest.encrypt(clear, algorithm, null);
        final String actual = underTest.decrypt(encrypted, algorithm, null);

        //then
        Assertions.assertEquals(clear, actual);
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.DECRYPT));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.RSA_OAEP_256, null));

        //then + exception
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(false);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenKeyIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null);
        underTest.setEnabled(false);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.RSA_OAEP_256, null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("stringSignSource")
    void testSignThenVerifyShouldReturnTrueWhenVerificationAndSignAreUsingTheSameData(
            final String clearSign, final String clearVerify, final SignatureAlgorithm algorithm, final int keySize) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keySize, null, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);

        //when
        final byte[] signature = underTest.signBytes(clearSign.getBytes(StandardCharsets.UTF_8), algorithm);
        final boolean actual = underTest.verifySignedBytes(clearVerify.getBytes(StandardCharsets.UTF_8), algorithm, signature);

        //then
        Assertions.assertEquals(clearSign.equals(clearVerify), actual);
    }

    @Test
    void testSignShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.VERIFY));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.signBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256));

        //then + exception
    }

    @Test
    void testVerifyShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.SIGN));
        underTest.setEnabled(true);

        //when
        final byte[] signature = underTest.signBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256, signature));

        //then + exception
    }

    @Test
    void testSignShouldThrowExceptionWhenOperationIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(false);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.signBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256));

        //then + exception
    }

    @Test
    void testVerifyShouldThrowExceptionWhenKeyIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        underTest.setEnabled(true);

        //when
        final byte[] signature = underTest.signBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256);
        underTest.setEnabled(false);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.verifySignedBytes(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.PS256, signature));

        //then + exception
    }
}
