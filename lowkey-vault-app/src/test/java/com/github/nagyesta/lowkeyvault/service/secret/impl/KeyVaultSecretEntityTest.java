package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.LOWKEY_VAULT;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
import static org.mockito.Mockito.mock;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

class KeyVaultSecretEntityTest {

    public static Stream<Arguments> invalidValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, null, null))
                .add(Arguments.of(null, mock(VaultFake.class), null))
                .add(Arguments.of(null, null, LOWKEY_VAULT))
                .add(Arguments.of(null, mock(VaultFake.class), LOWKEY_VAULT))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, null, LOWKEY_VAULT))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, mock(VaultFake.class), null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidValueProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedSecretEntityId id, final VaultFake vault, final String value) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultSecretEntity(id, vault, value, APPLICATION_JSON.toString()));

        //then + exception
    }
}
