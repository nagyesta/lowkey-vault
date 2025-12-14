package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateRestoreInput;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateImportInput;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import org.jspecify.annotations.Nullable;

public interface CertificateVaultFake
        extends BaseVaultFake<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity> {

    VersionedCertificateEntityId createCertificateVersion(String name, CertificateCreationInput input);

    VersionedCertificateEntityId importCertificateVersion(String name, CertificateImportInput input);

    void restoreCertificateVersion(VersionedCertificateEntityId versionedEntityId, CertificateRestoreInput input);

    @Nullable LifetimeActionPolicy lifetimeActionPolicy(CertificateEntityId certificateEntityId);

    void setLifetimeActionPolicy(LifetimeActionPolicy lifetimeActionPolicy);

    void regenerateCertificates();
}
