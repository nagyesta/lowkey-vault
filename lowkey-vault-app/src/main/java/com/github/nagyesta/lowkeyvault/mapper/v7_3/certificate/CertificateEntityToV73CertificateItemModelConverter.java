package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class CertificateEntityToV73CertificateItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel> {

    private final CertificateConverterRegistry registry;

    @Autowired
    public CertificateEntityToV73CertificateItemModelConverter(
            @NonNull final CertificateConverterRegistry registry) {
        super(KeyVaultCertificateItemModel::new, DeletedKeyVaultCertificateItemModel::new);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        register(registry);
    }

    protected void register(final CertificateConverterRegistry registry) {
        this.registry.registerItemConverter(this);
    }

    @Override
    protected <M extends KeyVaultCertificateItemModel> M mapActiveFields(
            final ReadOnlyKeyVaultCertificateEntity source, final M model, final URI vaultUri) {
        model.setCertificateId(convertCertificateId(source, vaultUri));
        model.setThumbprint(source.getThumbprint());
        model.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }

    protected String convertCertificateId(final ReadOnlyKeyVaultCertificateEntity source, final URI vaultUri) {
        return source.getId().asUriNoVersion(vaultUri).toString();
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
