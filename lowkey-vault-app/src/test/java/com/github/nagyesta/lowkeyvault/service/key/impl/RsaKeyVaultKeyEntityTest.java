package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultStubImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
        return Stream.<Arguments>builder()
                .add(Arguments.of(EMPTY))
                .add(Arguments.of(BLANK))
                .add(Arguments.of(DEFAULT_VAULT))
                .add(Arguments.of(LOCALHOST))
                .add(Arguments.of(LOWKEY_VAULT))
                .build();
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
    void testEncryptThenDecryptShouldReturnOriginalTextWhenCalled(final String clear) {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOWKEY_VAULT);
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultStub, MIN_RSA_KEY_SIZE, null, false);

        //when
        final byte[] encrypted = underTest.encrypt(clear.getBytes(StandardCharsets.UTF_8));
        final byte[] decrypted = underTest.decrypt(encrypted);
        final String actual = new String(decrypted, StandardCharsets.UTF_8);

        //then
        Assertions.assertEquals(clear, actual);
    }
}
