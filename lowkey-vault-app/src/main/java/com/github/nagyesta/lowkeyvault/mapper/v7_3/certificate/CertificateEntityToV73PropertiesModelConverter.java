package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BasePropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.SortedSet;

public class CertificateEntityToV73PropertiesModelConverter
        extends BasePropertiesModelConverter<VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity, CertificatePropertiesModel>
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePropertiesModel> {

    private final CertificateConverterRegistry registry;

    public CertificateEntityToV73PropertiesModelConverter(@lombok.NonNull final CertificateConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerPropertiesConverter(this);
    }

    @Override
    @NonNull
    public CertificatePropertiesModel convert(
            @org.springframework.lang.NonNull final ReadOnlyKeyVaultCertificateEntity source,
            @org.springframework.lang.NonNull final URI vaultUri) {
        return mapCommonFields(source, new CertificatePropertiesModel());
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
