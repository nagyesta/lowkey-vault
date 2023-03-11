package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

import java.time.OffsetDateTime;

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
        return createSecretVersion(entityId, value, contentType);
    }

    @Override
    public VersionedSecretEntityId createSecretVersion(
            @NonNull final VersionedSecretEntityId entityId, @NonNull final String value, final String contentType) {
        final KeyVaultSecretEntity secretEntity = new KeyVaultSecretEntity(entityId, vaultFake(), value, contentType);
        return addVersion(entityId, secretEntity);
    }

    @Override
    public VersionedSecretEntityId createSecretVersionForCertificate(
            @NonNull final VersionedSecretEntityId id,
            @NonNull final String value,
            @NonNull final CertContentType contentType,
            @NonNull final OffsetDateTime notBefore,
            @NonNull final OffsetDateTime expiry) {
        final VersionedSecretEntityId secretEntityId = createSecretVersion(id, value, contentType.getMimeType());
        setExpiry(secretEntityId, notBefore, expiry);
        setManaged(secretEntityId, true);
        setEnabled(secretEntityId, true);
        final KeyVaultSecretEntity secretEntity = getEntities().getEntity(secretEntityId, KeyVaultSecretEntity.class);
        secretEntity.setCreatedOn(notBefore);
        secretEntity.setUpdatedOn(notBefore);
        return secretEntityId;
    }
}
