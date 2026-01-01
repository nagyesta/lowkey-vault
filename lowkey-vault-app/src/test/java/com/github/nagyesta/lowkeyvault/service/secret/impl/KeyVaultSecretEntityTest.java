package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
import static org.mockito.Mockito.mock;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

class KeyVaultSecretEntityTest {

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var applicationJsonString = APPLICATION_JSON.toString();
        final var id = VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
        final var vault = mock(VaultFake.class);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultSecretEntity(id, vault, null, applicationJsonString));

        //then + exception
    }
}
