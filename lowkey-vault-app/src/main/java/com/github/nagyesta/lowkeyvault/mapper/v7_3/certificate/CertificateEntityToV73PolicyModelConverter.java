package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.springframework.stereotype.Component;

@Component("certificateEntityToV73PolicyModelConverter")
public class CertificateEntityToV73PolicyModelConverter extends BaseCertificateEntityToV73PolicyModelConverter {
    public CertificateEntityToV73PolicyModelConverter() {
        super(ReadOnlyKeyVaultCertificateEntity::getOriginalCertificatePolicy);
    }

}
