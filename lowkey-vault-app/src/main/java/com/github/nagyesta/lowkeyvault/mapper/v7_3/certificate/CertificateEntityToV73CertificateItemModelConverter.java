package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class CertificateEntityToV73CertificateItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel> {

    private final CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter;

    @Autowired
    public CertificateEntityToV73CertificateItemModelConverter(
            @NonNull final CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter) {
        super(KeyVaultCertificateItemModel::new, DeletedKeyVaultCertificateItemModel::new);
        this.certificateEntityToV73PropertiesModelConverter = certificateEntityToV73PropertiesModelConverter;
    }

    @Override
    protected <M extends KeyVaultCertificateItemModel> M mapActiveFields(
            final ReadOnlyKeyVaultCertificateEntity source, final M model, final URI vaultUri) {
        model.setCertificateId(convertCertificateId(source, vaultUri));
        model.setThumbprint(source.getThumbprint());
        model.setAttributes(certificateEntityToV73PropertiesModelConverter.convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }

    protected String convertCertificateId(final ReadOnlyKeyVaultCertificateEntity source, final URI vaultUri) {
        return source.getId().asUriNoVersion(vaultUri).toString();
    }
}
