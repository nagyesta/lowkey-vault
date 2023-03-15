package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.springframework.stereotype.Component;

@Component("certificateEntityToV73IssuancePolicyModelConverter")
public class CertificateEntityToV73IssuancePolicyModelConverter extends BaseCertificateEntityToV73PolicyModelConverter {
    public CertificateEntityToV73IssuancePolicyModelConverter() {
        super(ReadOnlyKeyVaultCertificateEntity::getIssuancePolicy);
    }

}
