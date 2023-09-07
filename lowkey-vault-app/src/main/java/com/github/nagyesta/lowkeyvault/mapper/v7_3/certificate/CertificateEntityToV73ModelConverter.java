package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class CertificateEntityToV73ModelConverter
        extends BaseRecoveryAwareConverter<VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateModel, DeletedKeyVaultCertificateModel> {

    private final CertificateConverterRegistry registry;

    @Autowired
    public CertificateEntityToV73ModelConverter(@NonNull final CertificateConverterRegistry registry) {
        super(KeyVaultCertificateModel::new, DeletedKeyVaultCertificateModel::new);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerModelConverter(this);
    }

    @Override
    protected <M extends KeyVaultCertificateModel> M mapActiveFields(
            final ReadOnlyKeyVaultCertificateEntity source, final M model, final URI vaultUri) {
        model.setId(source.getId().asUri(vaultUri).toString());
        model.setKid(source.getKid().asUri(vaultUri).toString());
        model.setSid(source.getSid().asUri(vaultUri).toString());
        model.setPolicy(registry.policyConverters(supportedVersions().last()).convert(source, vaultUri));
        model.setCertificate(source.getEncodedCertificate());
        model.setThumbprint(source.getThumbprint());
        model.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
