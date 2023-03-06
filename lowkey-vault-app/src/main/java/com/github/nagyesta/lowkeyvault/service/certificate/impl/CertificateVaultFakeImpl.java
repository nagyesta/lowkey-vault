package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CertificateVaultFakeImpl
        extends BaseVaultFakeImpl<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateEntity>
        implements CertificateVaultFake {

    private final ConcurrentMap<String, LifetimeActionPolicy> lifetimeActionPolicies = new ConcurrentHashMap<>();

    public CertificateVaultFakeImpl(@org.springframework.lang.NonNull final VaultFake vaultFake,
                                    @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                                    final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedCertificateEntityId createVersionedId(final String id, final String version) {
        return new VersionedCertificateEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public VersionedCertificateEntityId createCertificateVersion(
            @NonNull final String name, @NonNull final CertificateCreationInput input) {
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(name, input, vaultFake());
        return addVersion(entity.getId(), entity);
    }

    @Override
    public VersionedCertificateEntityId importCertificateVersion(
            @NonNull final String name, @NonNull final CertificateImportInput input) {
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(
                name, input, vaultFake());
        return addVersion(entity.getId(), entity);
    }

    @Override
    public void delete(@NonNull final CertificateEntityId entityId) {
        super.delete(entityId);
        vaultFake().keyVaultFake().delete(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().delete(toSecretEntityId(entityId));
    }

    @Override
    public void recover(@NonNull final CertificateEntityId entityId) {
        super.recover(entityId);
        vaultFake().keyVaultFake().recover(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().recover(toSecretEntityId(entityId));
    }

    @Override
    public void purge(@NonNull final CertificateEntityId entityId) {
        super.purge(entityId);
        vaultFake().keyVaultFake().purge(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().purge(toSecretEntityId(entityId));
    }

    @Override
    public LifetimeActionPolicy lifetimeActionPolicy(@NonNull final CertificateEntityId certificateEntityId) {
        purgeDeletedPolicies();
        return lifetimeActionPolicies.get(certificateEntityId.id());
    }

    @Override
    public void setLifetimeActionPolicy(@NonNull final LifetimeActionPolicy lifetimeActionPolicy) {
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = latestReadOnlyCertificateVersion(lifetimeActionPolicy.getId());
        lifetimeActionPolicy.validate(readOnlyEntity.getPolicy().getValidityMonths());
        final LifetimeActionPolicy existingPolicy = lifetimeActionPolicy(lifetimeActionPolicy.getId());
        if (existingPolicy == null) {
            lifetimeActionPolicies.put(lifetimeActionPolicy.getId().id(), lifetimeActionPolicy);
        } else {
            existingPolicy.setLifetimeActions(lifetimeActionPolicy.getLifetimeActions());
        }
    }

    private void purgeDeletedPolicies() {
        keepNamesReadyForRemoval(lifetimeActionPolicies.keySet())
                .forEach(lifetimeActionPolicies::remove);
    }

    private ReadOnlyKeyVaultCertificateEntity latestReadOnlyCertificateVersion(final CertificateEntityId certificateEntityId) {
        final VersionedCertificateEntityId latestVersionOfEntity = getEntities().getLatestVersionOfEntity(certificateEntityId);
        return getEntities().getReadOnlyEntity(latestVersionOfEntity);
    }

    private KeyEntityId toKeyEntityId(final CertificateEntityId entityId) {
        return new KeyEntityId(entityId.vault(), entityId.id());
    }

    private SecretEntityId toSecretEntityId(final CertificateEntityId entityId) {
        return new SecretEntityId(entityId.vault(), entityId.id());
    }
}
