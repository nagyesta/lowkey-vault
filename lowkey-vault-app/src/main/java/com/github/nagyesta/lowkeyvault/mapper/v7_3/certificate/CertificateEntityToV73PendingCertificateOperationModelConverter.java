package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.IssuerParameterModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CertificateEntityToV73PendingCertificateOperationModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel> {

    public @Nullable KeyVaultPendingCertificateModel convert(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI baseUri) {
        if (source == null) {
            return null;
        }
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

}
