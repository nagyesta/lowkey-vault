package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.URI;

public class CertificateEntityToV73CertificateVersionItemModelConverter
        extends CertificateEntityToV73CertificateItemModelConverter {

    @Autowired
    public CertificateEntityToV73CertificateVersionItemModelConverter(@NonNull final CertificateConverterRegistry registry) {
        super(registry);
    }

    @Override
    protected String convertCertificateId(final ReadOnlyKeyVaultCertificateEntity source, final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }

    @Override
    protected void register(final CertificateConverterRegistry registry) {
        registry.registerVersionedItemConverter(this);
    }
}
