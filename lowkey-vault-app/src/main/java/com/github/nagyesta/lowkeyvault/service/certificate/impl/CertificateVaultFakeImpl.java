package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

public class CertificateVaultFakeImpl
        extends BaseVaultFakeImpl<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateEntity>
        implements CertificateVaultFake {

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
                name, input.getCertificateData(), input.getCertificate(), input.getKeyData(), vaultFake());
        return addVersion(entity.getId(), entity);
    }
}
