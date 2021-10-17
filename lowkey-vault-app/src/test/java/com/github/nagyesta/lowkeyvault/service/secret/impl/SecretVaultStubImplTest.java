package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultStubImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstants.LOWKEY_VAULT;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecretVaultStubImplTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretVaultStubImpl(null, null, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithNullName() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultStubImpl underTest =
                new SecretVaultStubImpl(vaultStub, vaultStub.getRecoveryLevel(), vaultStub.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(null, LOWKEY_VAULT, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithNullValue() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultStubImpl underTest =
                new SecretVaultStubImpl(vaultStub, vaultStub.getRecoveryLevel(), vaultStub.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(SECRET_NAME_1, null, null));

        //then + exception
    }

    @Test
    void testCreateSecretVersionShouldCreateNewEntityWhenCalledWithValidInput() {
        //given
        final VaultStub vaultStub = new VaultStubImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultStubImpl underTest =
                new SecretVaultStubImpl(vaultStub, vaultStub.getRecoveryLevel(), vaultStub.getRecoverableDays());

        //when
        final VersionedSecretEntityId secretVersion = underTest.createSecretVersion(SECRET_NAME_1, LOWKEY_VAULT, null);

        //then
        final ReadOnlyKeyVaultSecretEntity actual = underTest.getEntities().getReadOnlyEntity(secretVersion);
        Assertions.assertNull(actual.getContentType());
        Assertions.assertEquals(LOWKEY_VAULT, actual.getValue());
        Assertions.assertEquals(secretVersion.asUri(), actual.getUri());
    }

    @Test
    void testCreateVersionedSecretEntityIdShouldCreateNewEntityIdWhenCalledWithValidInput() {
        //given
        final VaultStub vaultStub = mock(VaultStub.class);
        when(vaultStub.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final SecretVaultStubImpl underTest = new SecretVaultStubImpl(vaultStub, RecoveryLevel.PURGEABLE, null);

        //when
        final VersionedSecretEntityId actual = underTest.createVersionedId(SECRET_NAME_1, SECRET_VERSION_1);

        //then
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, actual);
    }
}
