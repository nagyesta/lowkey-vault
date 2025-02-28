package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.springframework.util.Assert;

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
            @NonNull final String secretName, final SecretCreateInput input) {
        final var entityId = new VersionedSecretEntityId(vaultFake().baseUri(), secretName);
        return createSecretVersion(entityId, input);
    }

    @Override
    public VersionedSecretEntityId createSecretVersion(
            @NonNull final VersionedSecretEntityId entityId, @NonNull final SecretCreateInput input) {
        Assert.isTrue(!input.isManaged() || (input.getExpiresOn() != null && input.getNotBefore() != null),
                "Managed secret (name=" + entityId.id() + ") must have notBefore and expiresOn parameters set!");
        Assert.isTrue(!input.isManaged() || input.getContentType() != null,
                "Managed secret (name=" + entityId.id() + ") must have the content type parameter set!");
        final var secretEntity = new KeyVaultSecretEntity(entityId, vaultFake(), input.getValue(), input.getContentType());
        final var secretEntityId = addVersion(entityId, secretEntity);
        addTags(secretEntityId, input.getTags());
        setExpiry(secretEntityId, input.getNotBefore(), input.getExpiresOn());
        setEnabled(secretEntityId, input.isEnabled());
        setManaged(secretEntityId, input.isManaged());
        setCreatedAndUpdatedOn(secretEntityId, input.getCreatedOn(), input.getUpdatedOn());
        return secretEntityId;
    }
}
