package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

public class CertificateEntityToV73PolicyModelConverter extends BaseCertificateEntityToV73PolicyModelConverter {

    private final CertificateConverterRegistry registry;

    @Autowired
    public CertificateEntityToV73PolicyModelConverter(@NonNull final CertificateConverterRegistry registry) {
        super(ReadOnlyKeyVaultCertificateEntity::getOriginalCertificatePolicy);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerPolicyConverter(this);
    }
}
