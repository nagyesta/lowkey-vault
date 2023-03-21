package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import lombok.NonNull;

public class CertificateEntityToV73IssuancePolicyModelConverter extends BaseCertificateEntityToV73PolicyModelConverter {

    private final CertificateConverterRegistry registry;

    public CertificateEntityToV73IssuancePolicyModelConverter(@NonNull final CertificateConverterRegistry registry) {
        super(ReadOnlyKeyVaultCertificateEntity::getIssuancePolicy);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerIssuancePolicyConverter(this);
    }
}
