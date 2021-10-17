package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultStubImpl;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;

public class SecretVaultStubImpl
        extends BaseVaultStubImpl<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, KeyVaultSecretEntity>
        implements SecretVaultStub {

    public SecretVaultStubImpl(@org.springframework.lang.NonNull final VaultStub vaultStub,
                               @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                               final Integer recoverableDays) {
        super(vaultStub, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedSecretEntityId createVersionedId(final String id, final String version) {
        return new VersionedSecretEntityId(vaultStub().baseUri(), id, version);
    }

    @Override
    public VersionedSecretEntityId createSecretVersion(
            @NonNull final String secretName, @NonNull final String value, final String contentType) {
        final VersionedSecretEntityId entityId = new VersionedSecretEntityId(vaultStub().baseUri(), secretName);
        final KeyVaultSecretEntity secretEntity = new KeyVaultSecretEntity(entityId, vaultStub(), value, contentType);
        return addVersion(entityId, secretEntity);
    }
}
