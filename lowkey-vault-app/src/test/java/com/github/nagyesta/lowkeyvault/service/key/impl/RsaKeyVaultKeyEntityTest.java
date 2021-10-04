package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultStubImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
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
                .add(Arguments.of(null, mock(VaultStub.class), null, null))
                .add(Arguments.of(null, null, MIN_RSA_KEY_SIZE, null))
                .add(Arguments.of(null, mock(VaultStub.class), MIN_RSA_KEY_SIZE, null))
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

    @ParameterizedTest
    @MethodSource("invalidValueProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedKeyEntityId id, final VaultStub vault, final Integer keySize, final BigInteger exponent) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RsaKeyVaultKeyEntity(id, vault, keySize, exponent, false));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("stringSource")
    void testEncryptThenDecryptShouldReturnOriginalTextWhenCalled(final String clear, final EncryptionAlgorithm algorithm) {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, algorithm.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(clear, algorithm, null, null, null);
        final String actual = underTest.decrypt(encrypted, algorithm, null, null, null);

        //then
        Assertions.assertEquals(clear, actual);
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.DECRYPT));
        underTest.setEnabled(true);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null, null, null));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenOperationIsNotAllowed() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null, null, null);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.RSA_OAEP_256, null, null, null));

        //then + exception
    }

    @Test
    void testEncryptShouldThrowExceptionWhenOperationIsNotEnabled() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        underTest.setEnabled(false);

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null, null, null));

        //then + exception
    }

    @Test
    void testDecryptShouldThrowExceptionWhenKeyIsNotEnabled() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, EncryptionAlgorithm.RSA_OAEP_256.getMinKeySize(), null, false);
        underTest.setOperations(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY));
        underTest.setEnabled(true);

        //when
        final byte[] encrypted = underTest.encrypt(DEFAULT_VAULT, EncryptionAlgorithm.RSA_OAEP_256, null, null, null);
        underTest.setEnabled(false);
        Assertions.assertThrows(IllegalStateException.class,
                () -> underTest.decrypt(encrypted, EncryptionAlgorithm.RSA_OAEP_256, null, null, null));

        //then + exception
    }
}
