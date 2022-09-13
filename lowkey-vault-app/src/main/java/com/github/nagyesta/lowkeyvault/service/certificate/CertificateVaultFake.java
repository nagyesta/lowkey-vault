package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;

public interface CertificateVaultFake
        extends BaseVaultFake<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity> {
    VersionedCertificateEntityId createCertificateVersion(String name, CertificateCreationInput input);
}
