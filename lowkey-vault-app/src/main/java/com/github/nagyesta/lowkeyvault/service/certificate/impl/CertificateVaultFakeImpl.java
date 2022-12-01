package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.springframework.lang.NonNull;

public class CertificateVaultFakeImpl
        extends BaseVaultFakeImpl<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateEntity>
        implements CertificateVaultFake {

    public CertificateVaultFakeImpl(@NonNull final VaultFake vaultFake,
                                    @NonNull final RecoveryLevel recoveryLevel,
                                    final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedCertificateEntityId createVersionedId(final String id, final String version) {
        return new VersionedCertificateEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public VersionedCertificateEntityId createCertificateVersion(final String name, final CertificateCreationInput input) {
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(name, input, vaultFake());
        return addVersion(entity.getId(), entity);
    }
}
