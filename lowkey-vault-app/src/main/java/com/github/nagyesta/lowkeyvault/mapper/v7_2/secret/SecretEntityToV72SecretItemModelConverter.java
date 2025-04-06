package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class SecretEntityToV72SecretItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedSecretEntityId,
        ReadOnlyKeyVaultSecretEntity, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel> {

    private final SecretConverterRegistry registry;

    @Autowired
    public SecretEntityToV72SecretItemModelConverter(@NonNull final SecretConverterRegistry registry) {
        super(KeyVaultSecretItemModel::new, DeletedKeyVaultSecretItemModel::new);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        register(registry);
    }

    protected void register(final SecretConverterRegistry registry) {
        registry.registerItemConverter(this);
    }

    @Override
    protected <M extends KeyVaultSecretItemModel> M mapActiveFields(
            final ReadOnlyKeyVaultSecretEntity source,
            final M model,
            final URI vaultUri) {
        model.setId(convertSecretId(source, vaultUri));
        model.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        model.setTags(source.getTags());
        if (source.isManaged()) {
            model.setManaged(true);
        }
        return model;
    }

    protected String convertSecretId(
            final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        return source.getId().asUriNoVersion(vaultUri).toString();
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.ALL_VERSIONS;
    }
}
