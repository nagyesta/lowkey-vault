package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

public class SecretVaultFakeImpl
        extends BaseVaultFakeImpl<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, KeyVaultSecretEntity>
        implements SecretVaultFake {

    public SecretVaultFakeImpl(@org.springframework.lang.NonNull final VaultFake vaultFake,
                               @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                               final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedSecretEntityId createVersionedId(final String id, final String version) {
        return new VersionedSecretEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public VersionedSecretEntityId createSecretVersion(
            @NonNull final String secretName, @NonNull final String value, final String contentType) {
        final VersionedSecretEntityId entityId = new VersionedSecretEntityId(vaultFake().baseUri(), secretName);
        final KeyVaultSecretEntity secretEntity = new KeyVaultSecretEntity(entityId, vaultFake(), value, contentType);
        return addVersion(entityId, secretEntity);
    }
}
