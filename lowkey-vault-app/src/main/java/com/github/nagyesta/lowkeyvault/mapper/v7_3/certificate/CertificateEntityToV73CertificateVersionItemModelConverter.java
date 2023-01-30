package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CertificateEntityToV73CertificateVersionItemModelConverter
        extends CertificateEntityToV73CertificateItemModelConverter {
    @Autowired
    public CertificateEntityToV73CertificateVersionItemModelConverter(
            @NonNull final CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter) {
        super(certificateEntityToV73PropertiesModelConverter);
    }

    @Override
    protected String convertCertificateId(final ReadOnlyKeyVaultCertificateEntity source, final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }
}
