package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_AES_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.Mockito.mock;

class AesKeyVaultKeyEntityTest {

    private static final byte[] IV = "_iv-param-value_".getBytes(StandardCharsets.UTF_8);

    public static Stream<Arguments> invalidValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, null))
                .add(Arguments.of(null, mock(VaultFake.class), null))
                .add(Arguments.of(null, null, MIN_AES_KEY_SIZE))
                .add(Arguments.of(null, mock(VaultFake.class), MIN_AES_KEY_SIZE))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, null, MIN_AES_KEY_SIZE))
                .build();
    }

    public static Stream<Arguments> stringSource() {
        return Arrays.stream(EncryptionAlgorithm.values())
                .filter(ea -> ea.isCompatible(KeyType.OCT_HSM))
                .flatMap(ea -> Stream.<Arguments>builder()
                        .add(Arguments.of(DEFAULT_VAULT, ea))
                        .add(Arguments.of(LOCALHOST, ea))
                        .add(Arguments.of(LOWKEY_VAULT, ea))
                        .build());
    }

    @ParameterizedTest
    @MethodSource("invalidValueProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedKeyEntityId id, final VaultFake vault, final Integer keySize) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AesKeyVaultKeyEntity(id, vault, keySize, false));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("stringSource")
    void testEncryptThenDecryptShouldReturnOriginalTextWhenCalled(final String clear, final EncryptionAlgorithm algorithm) {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, algorithm.getMinKeySize(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(clear, algorithm, IV);
        final String actual = underTest.decrypt(encrypted, algorithm, IV);

        //then
        Assertions.assertEquals(clear, actual);
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, null, false);
        underTest.setOperations(List.of(KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyType.OCT.getValidKeyParameters(Integer.class).first(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.A128CBC, IV));

        //then + exception
    }

    @Test
    void testEncryptShouldThrowExceptionWhenIvIsMissing() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, null));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenIvIsMissing() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyType.OCT.getValidKeyParameters(Integer.class).first(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.A128CBC, null));

        //then + exception
    }

    @Test
    void testEncryptShouldThrowExceptionWhenKeySizeDoesNotMatchAlgorithm() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A192CBC, IV));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenKeySizeDoesNotMatchAlgorithm() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyType.OCT.getValidKeyParameters(Integer.class).first(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.A192CBC, IV));

        //then + exception
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyType.OCT.getValidKeyParameters(Integer.class).first(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(false);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenKeyIsNotEnabled() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final AesKeyVaultKeyEntity underTest = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, KeyType.OCT.getValidKeyParameters(Integer.class).first(), false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.A128CBC, IV);
        underTest.setEnabled(false);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.A128CBC, IV));

        //then + exception
    }
}
