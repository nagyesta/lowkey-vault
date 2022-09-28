package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SecretEntityToV72SecretItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedSecretEntityId,
        ReadOnlyKeyVaultSecretEntity, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel> {

    private final SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter;

    @Autowired
    public SecretEntityToV72SecretItemModelConverter(
            @NonNull final SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter) {
        super(KeyVaultSecretItemModel::new, DeletedKeyVaultSecretItemModel::new);
        this.secretEntityToV72PropertiesModelConverter = secretEntityToV72PropertiesModelConverter;
    }

    @Override
    protected <M extends KeyVaultSecretItemModel> M mapActiveFields(
            final ReadOnlyKeyVaultSecretEntity source, final M model, final URI vaultUri) {
        model.setId(convertSecretId(source, vaultUri));
        model.setAttributes(secretEntityToV72PropertiesModelConverter.convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }

    protected String convertSecretId(final ReadOnlyKeyVaultSecretEntity source, final URI vaultUri) {
        return source.getId().asUriNoVersion(vaultUri).toString();
    }
}
