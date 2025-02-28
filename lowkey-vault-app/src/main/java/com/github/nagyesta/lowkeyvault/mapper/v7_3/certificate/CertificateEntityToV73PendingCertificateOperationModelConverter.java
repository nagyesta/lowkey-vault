package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.IssuerParameterModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class CertificateEntityToV73PendingCertificateOperationModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel> {

    private final CertificateConverterRegistry registry;

    @Autowired
    public CertificateEntityToV73PendingCertificateOperationModelConverter(@NonNull final CertificateConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerPendingOperationConverter(this);
    }

    public @org.springframework.lang.NonNull KeyVaultPendingCertificateModel convert(
            final @NonNull ReadOnlyKeyVaultCertificateEntity source, final @NonNull URI baseUri) {
        final var model = new KeyVaultPendingCertificateModel();
        model.setId(source.getId().asPendingOperationUri(baseUri).toString());
        model.setIssuer(new IssuerParameterModel(CertAuthorityType.SELF_SIGNED));
        model.setCsr(source.getEncodedCertificateSigningRequest());
        model.setCancellationRequested(false);
        model.setStatus("completed");
        model.setStatusDetails(null);
        model.setRequestId(source.getId().version());
        model.setTarget(source.getId().asUriNoVersion(baseUri).toString());
        return model;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
