package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component("certificateEntityToV73PropertiesModelConverter")
public class CertificateEntityToV73PropertiesModelConverter
        extends BaseEntityToV72PropertiesModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePropertiesModel> {

    @Override
    @NonNull
    public CertificatePropertiesModel convert(
            @org.springframework.lang.NonNull final ReadOnlyKeyVaultCertificateEntity source,
            @org.springframework.lang.NonNull final URI vaultUri) {
        return mapCommonFields(source, new CertificatePropertiesModel());
    }
}
