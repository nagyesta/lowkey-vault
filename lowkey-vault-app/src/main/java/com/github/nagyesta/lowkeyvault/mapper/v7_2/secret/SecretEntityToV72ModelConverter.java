package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class SecretEntityToV72ModelConverter
        extends BaseRecoveryAwareConverter<VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel,
        DeletedKeyVaultSecretModel> {
    private final SecretConverterRegistry registry;

    @Autowired
    public SecretEntityToV72ModelConverter(@NonNull final SecretConverterRegistry registry) {
        super(KeyVaultSecretModel::new, DeletedKeyVaultSecretModel::new);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerModelConverter(this);
    }

    @Override
    protected <M extends KeyVaultSecretModel> M mapActiveFields(
            final ReadOnlyKeyVaultSecretEntity source, final M model, final URI vaultUri) {
        model.setId(source.getId().asUri(vaultUri).toString());
        model.setContentType(source.getContentType());
        model.setValue(source.getValue());
        model.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        model.setTags(source.getTags());
        model.setManaged(source.isManaged());
        if (source.isManaged()) {
            final var id = source.getId();
            model.setKid(new VersionedKeyEntityId(id.vault(), id.id(), id.version()).asUri(vaultUri).toString());
        }
        return model;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.ALL_VERSIONS;
    }
}
