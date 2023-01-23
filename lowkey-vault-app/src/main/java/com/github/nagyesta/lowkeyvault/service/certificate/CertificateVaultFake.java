package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateImportInput;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import lombok.NonNull;

public interface CertificateVaultFake
        extends BaseVaultFake<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity> {
    VersionedCertificateEntityId createCertificateVersion(@NonNull String name, @NonNull CertificateCreationInput input);

    VersionedCertificateEntityId importCertificateVersion(@NonNull String name, @NonNull CertificateImportInput input);
}
