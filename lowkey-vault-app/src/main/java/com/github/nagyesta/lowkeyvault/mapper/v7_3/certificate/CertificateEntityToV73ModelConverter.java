package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component("certificateEntityToV73ModelConverter")
public class CertificateEntityToV73ModelConverter
        extends BaseRecoveryAwareConverter<VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateModel, DeletedKeyVaultCertificateModel> {
    private final CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter;
    private final CertificateEntityToV73PolicyModelConverter certificateEntityToV73PolicyModelConverter;

    @Autowired
    public CertificateEntityToV73ModelConverter(
            @NonNull final CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter,
            @NonNull final CertificateEntityToV73PolicyModelConverter certificateEntityToV73PolicyModelConverter) {
        super(KeyVaultCertificateModel::new, DeletedKeyVaultCertificateModel::new);
        this.certificateEntityToV73PropertiesModelConverter = certificateEntityToV73PropertiesModelConverter;
        this.certificateEntityToV73PolicyModelConverter = certificateEntityToV73PolicyModelConverter;
    }

    @Override
    protected <M extends KeyVaultCertificateModel> M mapActiveFields(
            final ReadOnlyKeyVaultCertificateEntity source, final M model, final URI vaultUri) {
        model.setId(source.getId().asUri(vaultUri).toString());
        model.setKid(source.getKid().asUri(vaultUri).toString());
        model.setSid(source.getSid().asUri(vaultUri).toString());
        model.setPolicy(certificateEntityToV73PolicyModelConverter.convert(source, vaultUri));
        model.setCertificate(source.getEncodedCertificate());
        model.setThumbprint(source.getThumbprint());
        model.setAttributes(certificateEntityToV73PropertiesModelConverter.convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }
}
