package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.IssuerParameterModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CertificateEntityToV73PendingCertificateOperationModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel> {

    public @org.springframework.lang.NonNull KeyVaultPendingCertificateModel convert(
            final @NonNull ReadOnlyKeyVaultCertificateEntity source, final @NonNull URI baseUri) {
        final KeyVaultPendingCertificateModel model = new KeyVaultPendingCertificateModel();
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
